package com.gerenciamentoit.shared.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NormalizerTest {

    @Test
    void normalizaCodigosEConverteVazioEmNulo() {
        assertThat(Normalizer.codigoOpcional("  pda-047 ")).isEqualTo("PDA-047");
        assertThat(Normalizer.codigoOpcional("   ")).isNull();
        assertThat(Normalizer.codigoOpcional(null)).isNull();
    }

    @Test
    void preservaTextoMasRemoveEspacosExternos() {
        assertThat(Normalizer.textoOpcional("  Honeywell CT40  ")).isEqualTo("Honeywell CT40");
    }
}
