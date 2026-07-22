package com.gerenciamentoit.acesso.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PapelTest {

    @Test
    void respeitaHierarquiaDeConcessao() {
        assertThat(Papel.GESTOR_TI.podeConceder(Papel.SUPERVISOR)).isTrue();
        assertThat(Papel.SUPERVISOR.podeConceder(Papel.LIDER)).isTrue();
        assertThat(Papel.LIDER.podeConceder(Papel.USUARIO)).isTrue();
        assertThat(Papel.LIDER.podeConceder(Papel.SUPERVISOR)).isFalse();
        assertThat(Papel.ANALISTA_TI.podeConceder(Papel.USUARIO)).isFalse();
    }
}
