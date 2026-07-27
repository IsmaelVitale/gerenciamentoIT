package com.gerenciamentoit.chamados.web;

import com.gerenciamentoit.chamados.application.ChamadoService;
import com.gerenciamentoit.shared.web.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/chamados")
public class ChamadoController {

    private final ChamadoService service;

    public ChamadoController(ChamadoService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CHAMADO_ABRIR')")
    public ResponseEntity<ChamadoService.ChamadoDetalhe> abrir(
            @Valid @RequestBody AbrirChamadoRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        String identificadorExterno = idempotencyKey == null || idempotencyKey.isBlank()
                ? request.identificadorExterno()
                : idempotencyKey;
        ChamadoService.AberturaChamado resultado = service.abrir(new ChamadoService.AbrirChamado(
                request.descricao(),
                request.telefoneContato(),
                identificadorExterno
        ));
        HttpStatus status = resultado.criado() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(resultado.chamado());
    }

    @GetMapping("/meus")
    @PreAuthorize("hasAuthority('CHAMADO_VISUALIZAR_PROPRIO')")
    public PageResponse<ChamadoService.ChamadoResumo> listarMeus(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho
    ) {
        int tamanhoSeguro = Math.min(Math.max(tamanho, 1), 100);
        Pageable pageable = PageRequest.of(
                Math.max(pagina, 0),
                tamanhoSeguro,
                Sort.by(Sort.Direction.DESC, "criadoEm")
        );
        Page<ChamadoService.ChamadoResumo> resultado = service.listarMeus(pageable);
        return PageResponse.from(resultado);
    }

    @GetMapping("/meus/{id}")
    @PreAuthorize("hasAuthority('CHAMADO_VISUALIZAR_PROPRIO')")
    public ChamadoService.ChamadoDetalhe buscarMeu(@PathVariable UUID id) {
        return service.buscarMeu(id);
    }

    @GetMapping("/meus/protocolo/{protocolo}")
    @PreAuthorize("hasAuthority('CHAMADO_VISUALIZAR_PROPRIO')")
    public ChamadoService.ChamadoDetalhe buscarMeuPorProtocolo(@PathVariable String protocolo) {
        return service.buscarMeuPorProtocolo(protocolo);
    }

    public record AbrirChamadoRequest(
            @NotBlank @Size(max = 4000) String descricao,
            @Size(max = 40) String telefoneContato,
            @Size(max = 200) String identificadorExterno
    ) {
    }
}
