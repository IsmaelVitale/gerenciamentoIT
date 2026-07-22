package com.gerenciamentoit.shared.security;

import com.gerenciamentoit.acesso.domain.OrigemAplicacao;
import com.gerenciamentoit.acesso.domain.Papel;
import com.gerenciamentoit.acesso.domain.Permissao;

import java.util.Set;
import java.util.UUID;

public record UsuarioAutenticado(
        UUID usuarioId,
        UUID sessaoId,
        String matricula,
        String nome,
        OrigemAplicacao origemAplicacao,
        Set<Papel> papeis,
        Set<Permissao> permissoes,
        Set<UUID> setores,
        Set<UUID> turnos
) {
    public UsuarioAutenticado {
        papeis = Set.copyOf(papeis);
        permissoes = Set.copyOf(permissoes);
        setores = Set.copyOf(setores);
        turnos = Set.copyOf(turnos);
    }

    public boolean possuiPapel(Papel papel) {
        return papeis.contains(papel);
    }

    public boolean possuiPermissao(Permissao permissao) {
        return permissoes.contains(permissao);
    }

    public boolean possuiSetor(UUID setorId) {
        return setorId != null && setores.contains(setorId);
    }
}
