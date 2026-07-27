require('dotenv').config();

const axios = require('axios');
const qrcode = require('qrcode-terminal');
const { Client, LocalAuth } = require('whatsapp-web.js');

const apiBaseUrl = (process.env.API_BASE_URL || 'http://localhost:8080/api').replace(/\/$/, '');
const apiTimeoutMs = Number(process.env.API_TIMEOUT_MS || 10000);
const simulationMode = String(process.env.SIMULATION_MODE || 'true').toLowerCase() === 'true';

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
    const normalizedText = originalText.toLowerCase();

    if (isHelpCommand(normalizedText)) {
        await message.reply(buildHelpMenu());
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

    await openTicket(message, description);
});

function isTicketCommand(text) {
    return /^!?chamado(?:\s|$)/i.test(text);
}

function isHelpCommand(text) {
    return ['ajuda', '!ajuda', 'menu', '!menu'].includes(text);
}

function buildHelpMenu() {
    return [
        '🤖 *Central de Suporte de TI*',
        '',
        'Comandos disponíveis:',
        '- *chamado [descrição]* — abre um novo chamado',
        '- *ajuda* — mostra este menu',
    ].join('\n');
}

async function openTicket(message, description) {
    try {
        await message.reply('🔄 Registrando seu chamado no sistema...');

        const payload = {
            telefoneUsuario: normalizePhone(message.from),
            problema: description,
            origem: 'WHATSAPP',
            identificadorMensagem: message.id?._serialized || null,
        };

        if (simulationMode) {
            console.log('Chamado simulado:', payload);
            await message.reply(
                '✅ Chamado recebido em modo de simulação. A integração real será ativada quando a API de chamados estiver disponível.',
            );
            return;
        }

        const response = await api.post('/api/v1/chamados', payload);
        const protocol = response.data?.protocolo || response.data?.id;

        if (!protocol) {
            throw new Error('A API nao retornou protocolo ou id do chamado.');
        }

        await message.reply(`✅ Chamado *#${protocol}* aberto com sucesso!`);
    } catch (error) {
        const status = error.response?.status;
        const detail = error.response?.data?.detail || error.response?.data?.message;

        console.error('Erro ao abrir chamado:', {
            message: error.message,
            status,
            detail,
        });

        await message.reply(
            '❌ Não foi possível registrar o chamado agora. Tente novamente mais tarde.',
        );
    }
}

function normalizePhone(messageFrom) {
    return messageFrom.replace(/@c\.us$/, '');
}

async function shutdown(signal) {
    console.log(`Recebido ${signal}. Encerrando bot...`);
    try {
        await client.destroy();
    } finally {
        process.exit(0);
    }
}

process.on('SIGINT', () => shutdown('SIGINT'));
process.on('SIGTERM', () => shutdown('SIGTERM'));

client.initialize().catch((error) => {
    console.error('Falha ao inicializar o bot:', error);
    process.exit(1);
});
