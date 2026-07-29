package com.gerenciamentoit.organizacao.web;

import com.gerenciamentoit.organizacao.application.ContextoOperacionalService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/me/contextos-operacionais")
public class ContextoOperacionalController {

    private final ContextoOperacionalService service;

    public ContextoOperacionalController(ContextoOperacionalService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ContextoOperacionalResponse> listar() {
        return service.listarDoUsuarioAtual().stream()
                .map(ContextoOperacionalResponse::from)
                .toList();
    }

    public record ContextoOperacionalResponse(
            SetorResponse setor,
            TurnoResponse turno
    ) {
        static ContextoOperacionalResponse from(
                ContextoOperacionalService.ContextoOperacional contexto
        ) {
            return new ContextoOperacionalResponse(
                    new SetorResponse(
                            contexto.setorId(),
                            contexto.setorCodigo(),
                            contexto.setorNome(),
                            contexto.cotaPdas()
                    ),
                    new TurnoResponse(
                            contexto.turnoId(),
                            contexto.turnoCodigo(),
                            contexto.turnoNome(),
                            contexto.turnoHoraInicio(),
                            contexto.turnoHoraFim()
                    )
            );
        }
    }

    public record SetorResponse(
            UUID id,
            String codigo,
            String nome,
            int cotaPdas
    ) {
    }

    public record TurnoResponse(
            UUID id,
            String codigo,
            String nome,
            LocalTime horaInicio,
            LocalTime horaFim
    ) {
    }
}
