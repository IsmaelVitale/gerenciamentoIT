package com.gerenciamentoit.organizacao.web;

import com.gerenciamentoit.organizacao.application.SetorService;
import com.gerenciamentoit.organizacao.domain.Setor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
@RequestMapping("/v1/setores")
public class SetorController {

    private final SetorService service;

    public SetorController(SetorService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SETOR_CRIAR')")
    public SetorResponse criar(@Valid @RequestBody CriarSetorRequest request) {
        return SetorResponse.from(service.criar(request.codigo(), request.nome(), request.cotaPdas()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SETOR_VISUALIZAR')")
    public List<SetorResponse> listar() {
        return service.listar().stream().map(SetorResponse::from).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SETOR_VISUALIZAR')")
    public SetorResponse buscar(@PathVariable UUID id) {
        return SetorResponse.from(service.buscar(id));
    }

    public record CriarSetorRequest(
            @NotBlank @Size(max = 40) String codigo,
            @NotBlank @Size(max = 120) String nome,
            @Min(0) int cotaPdas
    ) {
    }

    public record SetorResponse(
            UUID id,
            String codigo,
            String nome,
            int cotaPdas,
            boolean ativo,
            long versao
    ) {
        static SetorResponse from(Setor setor) {
            return new SetorResponse(
                    setor.getId(),
                    setor.getCodigo(),
                    setor.getNome(),
                    setor.getCotaPdas(),
                    setor.isAtivo(),
                    setor.getVersao()
            );
        }
    }
}
