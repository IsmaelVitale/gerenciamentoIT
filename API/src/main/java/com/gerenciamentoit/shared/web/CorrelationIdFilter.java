package com.gerenciamentoit.shared.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String recebido = request.getHeader(HEADER);
        String correlationId = normalizar(recebido);

        MDC.put("correlationId", correlationId);
        response.setHeader(HEADER, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("correlationId");
        }
    }

    private String normalizar(String recebido) {
        if (recebido == null) {
            return UUID.randomUUID().toString();
        }
        String candidato = recebido.trim();
        if (candidato.isEmpty()
                || candidato.length() > 100
                || !candidato.matches("[A-Za-z0-9._:-]+")) {
            return UUID.randomUUID().toString();
        }
        return candidato;
    }
}
