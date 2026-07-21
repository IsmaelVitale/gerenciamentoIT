// ==========================================
// MOTOR DE IMPRESSÃO NATIVO E RESOLUÇÃO DE NOMES
// ==========================================
const ImpressaoService = {

    /**
     * Resolve conflitos de nomes iguais (Xarás) e abrevia sobrenomes
     * baseando-se na lista completa de usuários do sistema.
     * * @param {string} nomeCompleto Nome do operador
     * @param {Array} usuariosGlobais Lista com todos os usuários do sistema
     * @param {number} nivelAbreviacao 1 = Padrão (Ex: Silva -> Sil.), 2 = Agressivo (Ex: Silva -> S.)
     */
    getNomeInteligente(nomeCompleto, usuariosGlobais = [], nivelAbreviacao = 1) {
        if (!nomeCompleto) return 'VAGO';

        const partes = nomeCompleto.trim().split(' ').filter(p => p.length > 0);
        const primeiroNome = partes[0].toUpperCase();

        // Se a pessoa só tem um nome cadastrado, retorna ele
        if (partes.length === 1) return primeiroNome;

        // Se não houver lista global para comparar, usa uma abreviação segura padrão
        if (!usuariosGlobais || !Array.isArray(usuariosGlobais) || usuariosGlobais.length === 0) {
            const sobrenomeFallback = partes[partes.length - 1].toUpperCase();
            if (nivelAbreviacao === 1) return `${primeiroNome} ${sobrenomeFallback.substring(0, 3)}.`;
            else return `${primeiroNome} ${sobrenomeFallback.substring(0, 1)}.`;
        }

        // Conta quantos usuários no banco têm exatamente esse mesmo PRIMEIRO NOME (O "Xará")
        const xaras = usuariosGlobais.filter(u =>
            (u.nome && u.nome.trim().toUpperCase().startsWith(primeiroNome + ' ')) ||
            (u.nome && u.nome.trim().toUpperCase() === primeiroNome)
        );

        // Se for a única pessoa com esse nome na empresa inteira, imprimimos só o primeiro nome!
        if (xaras.length <= 1 && nivelAbreviacao === 1) {
            return primeiroNome;
        }

        // Se encontrou Xará (ou se fomos forçados ao Nível 2 pelo limite), pegamos o último nome
        const ultimoNome = partes[partes.length - 1].toUpperCase();

        // Nível 1 de abreviação: "ANA CAR."
        if (nivelAbreviacao === 1) {
            return `${primeiroNome} ${ultimoNome.substring(0, 3)}.`;
        }
        // Nível 2 de abreviação (Mais agressivo para caber na etiqueta): "ANA C."
        else {
            return `${primeiroNome} ${ultimoNome.substring(0, 1)}.`;
        }
    },

    /**
     * Lógica de Negócio: Transforma os dados da PDA na string padrão
     * garantindo que NUNCA ultrapasse 24 caracteres!
     */
    gerarTextoEtiqueta(pda, usuariosGlobais = []) {
        if (!pda) return "( ERRO DE LEITURA )";

        const t1 = pda.donoTurno1;
        const t2 = pda.donoTurno2;

        // Se não tem donos, imprime o Setor Alocado ou LIVRE
        if (!t1 && !t2) {
            const setorFormatado = pda.setorAlocado ? pda.setorAlocado.toUpperCase().substring(0, 20) : 'LIVRE';
            return `( ${setorFormatado} )`;
        }

        let textoFinal = "";

        // TENTATIVA 1: Modo de abreviação normal (Nível 1)
        let nome1 = t1 ? this.getNomeInteligente(t1.nome, usuariosGlobais, 1) : '';
        let nome2 = t2 ? this.getNomeInteligente(t2.nome, usuariosGlobais, 1) : '';

        if (t1 && t2) {
            textoFinal = `( ${nome1} / ${nome2} )`;
        } else if (t1) {
            textoFinal = `( ${nome1} T1 )`;
        } else if (t2) {
            textoFinal = `( ${nome2} T2 )`;
        }

        // TENTATIVA 2: Passou de 24 caracteres? Entra no Modo Agressivo (Nível 2)
        if (textoFinal.length > 24) {
            nome1 = t1 ? this.getNomeInteligente(t1.nome, usuariosGlobais, 2) : '';
            nome2 = t2 ? this.getNomeInteligente(t2.nome, usuariosGlobais, 2) : '';

            if (t1 && t2) {
                textoFinal = `( ${nome1} / ${nome2} )`;
            } else if (t1) {
                textoFinal = `( ${nome1} T1 )`;
            } else if (t2) {
                textoFinal = `( ${nome2} T2 )`;
            }
        }

        // TENTATIVA 3: Ainda está acima de 24 caracteres? Corta os nomes "no osso"
        if (textoFinal.length > 24) {
            if (t1 && t2) {
                // Limita cada nome a 8 letras para garantir que caiba com as barras e os parênteses
                let corte1 = nome1.substring(0, 8);
                let corte2 = nome2.substring(0, 8);
                textoFinal = `( ${corte1}. / ${corte2}. )`;
            } else {
                let corteUnico = (nome1 || nome2).substring(0, 16);
                textoFinal = `( ${corteUnico}. ${t1 ? 'T1' : 'T2'} )`;
            }
        }

        return textoFinal;
    },

    /**
     * Prepara a "Etiqueta Fantasma" oculta e dispara a janela nativa do Windows
     */
    imprimir(textoEtiqueta) {
        const printArea = document.getElementById('etiqueta-print-area');
        const printText = document.getElementById('etiqueta-print-texto');

        if (!printArea || !printText) {
            console.warn("Aviso: As divs da Etiqueta Fantasma não foram encontradas nesta página.");
            return;
        }

        // Injeta o texto na div oculta e manda imprimir
        printText.innerText = textoEtiqueta;
        window.print();
    }
};