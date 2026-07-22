package com.gerenciamentoit.acesso.security;

import com.gerenciamentoit.acesso.application.SessaoService;
import com.gerenciamentoit.shared.security.UsuarioAutenticado;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SessaoAuthenticationFilter extends OncePerRequestFilter {

    private final SessaoService sessaoService;

    public SessaoAuthenticationFilter(SessaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7).trim();
            sessaoService.autenticar(token).ifPresent(principal -> autenticar(principal, token));
        }
        filterChain.doFilter(request, response);
    }

    private void autenticar(UsuarioAutenticado principal, String token) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        principal.papeis().forEach(papel ->
                authorities.add(new SimpleGrantedAuthority("ROLE_" + papel.name())));
        principal.permissoes().forEach(permissao ->
                authorities.add(new SimpleGrantedAuthority(permissao.name())));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, token, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
