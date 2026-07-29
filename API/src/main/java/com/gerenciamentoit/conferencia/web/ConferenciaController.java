package com.gerenciamentoit.conferencia.web;

import com.gerenciamentoit.conferencia.application.ConferenciaService;
import com.gerenciamentoit.conferencia.domain.TipoConferencia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/conferencias")
@PreAuthorize("hasAuthority('CONFERENCIA_EXECUTAR')")
public class ConferenciaController {

    private final ConferenciaService service;

    public ConferenciaController(ConferenciaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConferenciaService.ConferenciaDetalhe abrir(
            @Valid @RequestBody AbrirConferenciaRequest request
    ) {
        return service.abrir(new ConferenciaService.AbrirConferencia(
                request.setorId(),
                request.turnoId(),
                request.tipo()
        ));
    }

    @GetMapping("/{id}")
    public ConferenciaService.ConferenciaDetalhe buscar(@PathVariable UUID id) {
        return service.buscar(id);
    }

    @GetMapping("/{id}/itens")
    public List<ConferenciaService.ItemConferencia> listarItens(@PathVariable UUID id) {
        return service.listarItens(id);
    }

    @GetMapping("/{id}/leituras")
    public List<ConferenciaService.LeituraRegistrada> listarLeituras(@PathVariable UUID id) {
        return service.listarLeituras(id);
    }

    @PostMapping("/{id}/leituras")
    public ConferenciaService.LeituraComResumo registrarLeitura(
            @PathVariable UUID id,
            @Valid @RequestBody RegistrarLeituraRequest request
    ) {
        return service.registrarLeitura(id, request.codigo());
    }

    @PostMapping("/{id}/conclusoes")
    public ConferenciaService.ConferenciaDetalhe concluir(@PathVariable UUID id) {
        return service.concluir(id);
    }

    public record AbrirConferenciaRequest(
            @NotNull UUID setorId,
            @NotNull UUID turnoId,
            @NotNull TipoConferencia tipo
    ) {
    }

    public record RegistrarLeituraRequest(
            @NotBlank @Size(max = 120) String codigo
    ) {
    }
}
