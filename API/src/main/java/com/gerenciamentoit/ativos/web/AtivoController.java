package com.gerenciamentoit.ativos.web;

import com.gerenciamentoit.ativos.application.AtivoService;
import com.gerenciamentoit.ativos.domain.DisponibilidadeAtivo;
import com.gerenciamentoit.ativos.domain.SituacaoPatrimonial;
import com.gerenciamentoit.shared.web.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/ativos")
public class AtivoController {

    private final AtivoService service;

    public AtivoController(AtivoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ATIVO_CADASTRAR')")
    public AtivoService.AtivoDetalhe cadastrar(@Valid @RequestBody CadastrarAtivoRequest request) {
        return service.cadastrar(new AtivoService.CadastrarAtivo(
                request.tipoAtivoId(),
                request.numeroSerie(),
                request.patrimonio(),
                request.fabricante(),
                request.modelo(),
                request.observacao()
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ATIVO_VISUALIZAR')")
    public AtivoService.AtivoDetalhe buscar(@PathVariable UUID id) {
        return service.buscar(id);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ATIVO_VISUALIZAR')")
    public PageResponse<AtivoService.AtivoResumo> pesquisar(
            @RequestParam(required = false) String numeroSerie,
            @RequestParam(required = false) String patrimonio,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) SituacaoPatrimonial situacaoPatrimonial,
            @RequestParam(required = false) DisponibilidadeAtivo disponibilidade,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho
    ) {
        int tamanhoSeguro = Math.min(Math.max(tamanho, 1), 100);
        Pageable pageable = PageRequest.of(
                Math.max(pagina, 0),
                tamanhoSeguro,
                Sort.by(Sort.Direction.DESC, "criadoEm")
        );
        Page<AtivoService.AtivoResumo> resultado = service.pesquisar(
                new AtivoService.FiltroAtivo(
                        numeroSerie,
                        patrimonio,
                        tipo,
                        situacaoPatrimonial,
                        disponibilidade
                ),
                pageable
        );
        return PageResponse.from(resultado);
    }

    @PostMapping("/{id}/liberacoes")
    @PreAuthorize("hasAuthority('ATIVO_LIBERAR')")
    public AtivoService.AtivoDetalhe liberar(@PathVariable UUID id) {
        return service.liberar(id);
    }

    @PostMapping("/{id}/correcoes-identificacao")
    @PreAuthorize("hasAuthority('ATIVO_CORRIGIR_IDENTIFICACAO')")
    public AtivoService.AtivoDetalhe corrigirIdentificacao(
            @PathVariable UUID id,
            @Valid @RequestBody CorrigirIdentificacaoRequest request
    ) {
        return service.corrigirIdentificacao(id, new AtivoService.CorrigirIdentificacao(
                request.numeroSerie(),
                request.patrimonio(),
                request.removerPatrimonio(),
                request.motivo()
        ));
    }

    public record CadastrarAtivoRequest(
            @NotNull UUID tipoAtivoId,
            @NotBlank @Size(max = 120) String numeroSerie,
            @Size(max = 80) String patrimonio,
            @Size(max = 120) String fabricante,
            @Size(max = 120) String modelo,
            @Size(max = 1000) String observacao
    ) {
    }

    public record CorrigirIdentificacaoRequest(
            @NotBlank @Size(max = 120) String numeroSerie,
            @Size(max = 80) String patrimonio,
            boolean removerPatrimonio,
            @NotBlank @Size(max = 500) String motivo
    ) {
    }
}
