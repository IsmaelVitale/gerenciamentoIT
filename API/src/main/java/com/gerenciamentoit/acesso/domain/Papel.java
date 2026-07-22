package com.gerenciamentoit.acesso.domain;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static com.gerenciamentoit.acesso.domain.Permissao.*;

public enum Papel {
    USUARIO(EnumSet.noneOf(Permissao.class)),

    LIDER(EnumSet.of(
            SETOR_VISUALIZAR,
            TURNO_VISUALIZAR,
            USUARIO_CRIAR,
            USUARIO_VISUALIZAR,
            USUARIO_ATRIBUIR_ACESSO,
            TIPO_ATIVO_VISUALIZAR,
            ATIVO_VISUALIZAR
    )),

    SUPERVISOR(EnumSet.of(
            SETOR_CRIAR,
            SETOR_VISUALIZAR,
            TURNO_CRIAR,
            TURNO_VISUALIZAR,
            USUARIO_CRIAR,
            USUARIO_VISUALIZAR,
            USUARIO_ATRIBUIR_ACESSO,
            TIPO_ATIVO_VISUALIZAR,
            ATIVO_VISUALIZAR
    )),

    ANALISTA_TI(EnumSet.of(
            SETOR_VISUALIZAR,
            TURNO_VISUALIZAR,
            USUARIO_VISUALIZAR,
            TIPO_ATIVO_GERENCIAR,
            TIPO_ATIVO_VISUALIZAR,
            ATIVO_CADASTRAR,
            ATIVO_VISUALIZAR,
            ATIVO_LIBERAR,
            ATIVO_CORRIGIR_IDENTIFICACAO
    )),

    GESTOR_TI(EnumSet.allOf(Permissao.class));

    private final Set<Permissao> permissoes;

    Papel(Set<Permissao> permissoes) {
        this.permissoes = permissoes.isEmpty()
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(permissoes));
    }

    public Set<Permissao> getPermissoes() {
        return permissoes;
    }

    public boolean podeConceder(Papel destino) {
        return switch (this) {
            case GESTOR_TI -> true;
            case SUPERVISOR -> destino == LIDER || destino == USUARIO;
            case LIDER -> destino == USUARIO;
            case ANALISTA_TI, USUARIO -> false;
        };
    }
}
