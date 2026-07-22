package com.gerenciamentoit.shared.security;

import com.gerenciamentoit.shared.error.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ContextoAutenticacao {

    public UsuarioAutenticado atual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new ForbiddenException("SESSAO_NAO_IDENTIFICADA", "Nenhuma sessao autenticada foi encontrada.");
        }
        return usuario;
    }
}
