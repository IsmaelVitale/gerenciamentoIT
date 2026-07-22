package com.gerenciamentoit.ativos.web;

import com.gerenciamentoit.ativos.application.TipoAtivoService;
import com.gerenciamentoit.ativos.domain.TipoAtivo;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/tipos-ativo")
public class TipoAtivoController {

    private final TipoAtivoService service;

    public TipoAtivoController(TipoAtivoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('TIPO_ATIVO_GERENCIAR')")
    public TipoAtivoResponse criar(@Valid @RequestBody CriarTipoAtivoRequest request) {
        return TipoAtivoResponse.from(service.criar(request.codigo(), request.nome(), request.controlaPool()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TIPO_ATIVO_VISUALIZAR')")
    public List<TipoAtivoResponse> listar() {
        return service.listar().stream().map(TipoAtivoResponse::from).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TIPO_ATIVO_VISUALIZAR')")
    public TipoAtivoResponse buscar(@PathVariable UUID id) {
        return TipoAtivoResponse.from(service.buscar(id));
    }

    public record CriarTipoAtivoRequest(
            @NotBlank @Size(max = 60) String codigo,
            @NotBlank @Size(max = 120) String nome,
            boolean controlaPool
    ) {
    }

    public record TipoAtivoResponse(
            UUID id,
            String codigo,
            String nome,
            boolean controlaPool,
            boolean ativo,
            long versao
    ) {
        static TipoAtivoResponse from(TipoAtivo tipo) {
            return new TipoAtivoResponse(
                    tipo.getId(),
                    tipo.getCodigo(),
                    tipo.getNome(),
                    tipo.isControlaPool(),
                    tipo.isAtivo(),
                    tipo.getVersao()
            );
        }
    }
}
