require('dotenv').config();

const axios = require('axios');
const qrcode = require('qrcode-terminal');
const { Client, LocalAuth } = require('whatsapp-web.js');

const apiBaseUrl = (process.env.API_BASE_URL || 'http://localhost:8080/api').replace(/\/$/, '');
const apiTimeoutMs = positiveInteger(process.env.API_TIMEOUT_MS, 10000);
const pendingTicketTtlMs = positiveInteger(process.env.PENDING_TICKET_TTL_MS, 300000);
const simulationMode = String(process.env.SIMULATION_MODE || 'true').toLowerCase() === 'true';
const pendingTickets = new Map();

const api = axios.create({
    baseURL: apiBaseUrl,
    timeout: apiTimeoutMs,
    headers: {
        'Content-Type': 'application/json',
    },
});

const client = new Client({
    authStrategy: new LocalAuth({
        dataPath: process.env.WHATSAPP_SESSION_PATH || '.wwebjs_auth',
    }),
    puppeteer: {
        headless: true,
        args: ['--no-sandbox', '--disable-setuid-sandbox', '--disable-dev-shm-usage'],
    },
});

client.on('qr', (qr) => {
    console.log('\nEscaneie o QR Code abaixo com o WhatsApp:\n');
    qrcode.generate(qr, { small: true });
});

client.on('authenticated', () => {
    console.log('Sessao do WhatsApp autenticada.');
});

client.on('ready', () => {
    console.log(`Bot pronto. API: ${apiBaseUrl}. Simulacao: ${simulationMode}.`);
});

client.on('auth_failure', (message) => {
    console.error('Falha de autenticacao do WhatsApp:', message);
});

client.on('disconnected', (reason) => {
    console.warn('Bot desconectado do WhatsApp:', reason);
});

client.on('message', async (message) => {
    if (message.isStatus || message.fromMe || typeof message.body !== 'string') {
        return;
    }

    const originalText = message.body.trim();
    if (!originalText) {
        return;
    }

    const normalizedText = originalText.toLowerCase();
    cleanupExpiredPendingTickets();

    if (isGroupMessage(message.from)) {
        if (isTicketCommand(normalizedText) || isHelpCommand(normalizedText)) {
            await message.reply('Por privacidade, abra o chamado em uma conversa direta com o bot.');
        }
        return;
    }

    if (isCancelCommand(normalizedText)) {
        const removed = pendingTickets.delete(message.from);
        await message.reply(removed ? 'Solicitacao cancelada.' : 'Nao existe solicitacao pendente.');
        return;
    }

    if (isHelpCommand(normalizedText)) {
        await message.reply(buildHelpMenu());
        return;
    }

    const pendingTicket = pendingTickets.get(message.from);
    if (pendingTicket && !isTicketCommand(normalizedText)) {
        await completePendingTicket(message, originalText, pendingTicket);
        return;
    }

    if (!isTicketCommand(normalizedText)) {
        return;
    }

    const description = originalText.replace(/^!?chamado\b\s*/i, '').trim();
    if (!description) {
        await message.reply(
            'Descreva o problema. Exemplo: *chamado leitor de codigo de barras travado*',
        );
        return;
    }

    pendingTickets.set(message.from, {
        description,
        externalMessageId: serializedMessageId(message),
        expiresAt: Date.now() + pendingTicketTtlMs,
    });

    await message.reply(
        'Para vincular o chamado ao seu cadastro, informe agora a sua *matricula*.\n' +
        'Envie *cancelar* para desistir.',
    );
});

function isTicketCommand(text) {
    return /^!?chamado(?:\s|$)/i.test(text);
}

function isHelpCommand(text) {
    return ['ajuda', '!ajuda', 'menu', '!menu'].includes(text);
}

function isCancelCommand(text) {
    return ['cancelar', '!cancelar'].includes(text);
}

function isGroupMessage(messageFrom) {
    return messageFrom.endsWith('@g.us');
}

function buildHelpMenu() {
    return [
        '🤖 *Central de Suporte de TI*',
        '',
        'Comandos disponiveis:',
        '- *chamado [descricao]* — inicia a abertura de um chamado',
        '- *cancelar* — cancela a solicitacao pendente',
        '- *ajuda* — mostra este menu',
        '',
        'Depois da descricao, o bot solicitara sua matricula.',
    ].join('\n');
}

async function completePendingTicket(message, matriculaInformada, pendingTicket) {
    if (Date.now() > pendingTicket.expiresAt) {
        pendingTickets.delete(message.from);
        await message.reply('A solicitacao expirou. Envie novamente *chamado [descricao]*.');
        return;
    }

    const matricula = matriculaInformada.trim();
    if (!matricula || matricula.length > 80) {
        await message.reply('Informe uma matricula valida ou envie *cancelar*.');
        return;
    }

    pendingTickets.delete(message.from);
    await message.reply('🔄 Validando sua matricula e registrando o chamado...');

    if (simulationMode) {
        console.log('Chamado simulado:', {
            description: pendingTicket.description,
            externalMessageId: pendingTicket.externalMessageId,
        });
        await message.reply(
            '✅ Chamado recebido em modo de simulacao. Nenhum registro foi criado na API.',
        );
        return;
    }

    let token;
    try {
        const sessionResponse = await api.post('/v1/sessoes', {
            matricula,
            origemAplicacao: 'WHATSAPP',
        });
        token = sessionResponse.data?.token;
        if (!token) {
            throw new Error('A API nao retornou o token da sessao.');
        }

        const phone = await resolvePhone(message);
        const response = await api.post(
            '/v1/chamados',
            {
                descricao: pendingTicket.description,
                telefoneContato: phone,
                identificadorExterno: pendingTicket.externalMessageId,
            },
            {
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Idempotency-Key': pendingTicket.externalMessageId,
                },
            },
        );
        const protocol = response.data?.protocolo;
        if (!protocol) {
            throw new Error('A API nao retornou o protocolo do chamado.');
        }

        await message.reply(`✅ Chamado *#${protocol}* aberto com sucesso!`);
    } catch (error) {
        const apiCode = error.response?.data?.codigo;
        const apiMessage = error.response?.data?.mensagem;

        console.error('Erro ao abrir chamado:', {
            message: error.message,
            status: error.response?.status,
            apiCode,
            apiMessage,
        });

        if (apiCode === 'MATRICULA_NAO_ENCONTRADA') {
            restorePendingTicket(message.from, pendingTicket);
            await message.reply('❌ Matricula nao encontrada. Confira o valor ou envie *cancelar*.');
            return;
        }
        if (apiCode === 'USUARIO_INATIVO') {
            await message.reply('❌ Seu cadastro esta inativo. Procure a equipe de T.I.');
            return;
        }

        restorePendingTicket(message.from, pendingTicket);
        await message.reply(
            '❌ Nao foi possivel registrar o chamado agora. Tente informar a matricula novamente ou envie *cancelar*.',
        );
    } finally {
        if (token) {
            await revokeSession(token);
        }
    }
}

function restorePendingTicket(chatId, pendingTicket) {
    if (Date.now() <= pendingTicket.expiresAt) {
        pendingTickets.set(chatId, pendingTicket);
    }
}

async function resolvePhone(message) {
    try {
        const contact = await message.getContact();
        const phone = String(contact?.number || '').replace(/\D/g, '');
        return phone || null;
    } catch (error) {
        console.warn('Nao foi possivel identificar o telefone de contato:', error.message);
        const fallback = String(message.from || '').replace(/\D/g, '');
        return fallback || null;
    }
}

async function revokeSession(token) {
    try {
        await api.delete('/v1/sessoes/atual', {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });
    } catch (error) {
        console.warn('Nao foi possivel revogar a sessao temporaria:', error.message);
    }
}

function serializedMessageId(message) {
    return message.id?._serialized || `whatsapp-${Date.now()}`;
}

function cleanupExpiredPendingTickets() {
    const now = Date.now();
    for (const [chatId, pendingTicket] of pendingTickets.entries()) {
        if (pendingTicket.expiresAt < now) {
            pendingTickets.delete(chatId);
        }
    }
}

function positiveInteger(value, fallback) {
    const parsed = Number(value);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback;
}

async function shutdown(signal) {
    console.log(`Recebido ${signal}. Encerrando bot...`);
    try {
        await client.destroy();
    } finally {
        process.exit(0);
    }
}

const cleanupTimer = setInterval(
    cleanupExpiredPendingTickets,
    Math.min(pendingTicketTtlMs, 60000),
);
cleanupTimer.unref();

process.on('SIGINT', () => shutdown('SIGINT'));
process.on('SIGTERM', () => shutdown('SIGTERM'));

client.initialize().catch((error) => {
    console.error('Falha ao inicializar o bot:', error);
    process.exit(1);
});
