// ==========================================
// MOTOR DE IMPRESSÃO NATIVO (HTML/CSS -> WINDOWS)
// ==========================================
const ImpressaoService = {

    /**
     * Pega o Primeiro e o Último nome para caber na etiqueta e evitar xarás.
     * Ex: "JOÃO BATISTA SILVA" -> "JOÃO SILVA"
     */
    getNomeCurto(nomeCompleto) {
        if (!nomeCompleto) return 'VAGO';
        const partes = nomeCompleto.trim().split(' ').filter(p => p.length > 0);
        if (partes.length === 1) return partes[0].toUpperCase();
        return (partes[0] + ' ' + partes[partes.length - 1]).toUpperCase();
    },

    /**
     * Lógica de Negócio: Transforma os dados da PDA na string padrão
     */
    gerarTextoEtiqueta(pda) {
        if (!pda) return "( ERRO DE LEITURA )";

        const t1 = pda.donoTurno1;
        const t2 = pda.donoTurno2;

        if (!t1 && !t2) {
            // Imprimir o nome do Setor Alocado na etiqueta
            const setorFormatado = pda.setorAlocado ? pda.setorAlocado.toUpperCase().substring(0, 15) : 'LIVRE';
            return `( ${setorFormatado} )`;
        } else if (!t2) {
            return `( ${this.getNomeCurto(t1.nome)} T1 )`;
        } else if (!t1) {
            return `( ${this.getNomeCurto(t2.nome)} T2 )`;
        } else {
            return `( ${this.getNomeCurto(t1.nome)} / ${this.getNomeCurto(t2.nome)} )`;
        }
    },

    /**
     * Prepara a "Etiqueta Fantasma" oculta e dispara a janela do Windows
     */
    imprimir(textoEtiqueta) {
        const printArea = document.getElementById('etiqueta-print-area');
        const printText = document.getElementById('etiqueta-print-texto');

        if (!printArea || !printText) {
            console.warn("Aviso: As divs da Etiqueta Fantasma não foram encontradas nesta página.");
            return;
        }

        printText.innerText = textoEtiqueta;
        window.print();
    }
};