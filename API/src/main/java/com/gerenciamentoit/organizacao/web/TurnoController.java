package com.gerenciamentoit.organizacao.web;

import com.gerenciamentoit.organizacao.application.TurnoService;
import com.gerenciamentoit.organizacao.domain.Turno;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/turnos")
public class TurnoController {

    private final TurnoService service;

    public TurnoController(TurnoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('TURNO_CRIAR')")
    public TurnoResponse criar(@Valid @RequestBody CriarTurnoRequest request) {
        return TurnoResponse.from(service.criar(request.codigo(), request.nome(), request.horaInicio(), request.horaFim()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TURNO_VISUALIZAR')")
    public List<TurnoResponse> listar() {
        return service.listar().stream().map(TurnoResponse::from).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TURNO_VISUALIZAR')")
    public TurnoResponse buscar(@PathVariable UUID id) {
        return TurnoResponse.from(service.buscar(id));
    }

    public record CriarTurnoRequest(
            @NotBlank @Size(max = 40) String codigo,
            @NotBlank @Size(max = 120) String nome,
            LocalTime horaInicio,
            LocalTime horaFim
    ) {
    }

    public record TurnoResponse(
            UUID id,
            String codigo,
            String nome,
            LocalTime horaInicio,
            LocalTime horaFim,
            boolean ativo,
            long versao
    ) {
        static TurnoResponse from(Turno turno) {
            return new TurnoResponse(
                    turno.getId(),
                    turno.getCodigo(),
                    turno.getNome(),
                    turno.getHoraInicio(),
                    turno.getHoraFim(),
                    turno.isAtivo(),
                    turno.getVersao()
            );
        }
    }
}
