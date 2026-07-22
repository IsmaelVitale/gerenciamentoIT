package com.gerenciamentoit.acesso.web;

import com.gerenciamentoit.acesso.application.UsuarioService;
import com.gerenciamentoit.acesso.domain.AtribuicaoAcesso;
import com.gerenciamentoit.acesso.domain.Papel;
import com.gerenciamentoit.acesso.domain.Usuario;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USUARIO_CRIAR')")
    public UsuarioResponse criar(@Valid @RequestBody CriarUsuarioRequest request) {
        return UsuarioResponse.from(service.criar(request.matricula(), request.nome()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public UsuarioDetalheResponse buscar(@PathVariable UUID id) {
        Usuario usuario = service.buscar(id);
        return UsuarioDetalheResponse.from(usuario, service.listarAtribuicoes(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USUARIO_VISUALIZAR')")
    public UsuarioDetalheResponse buscarPorMatricula(@RequestParam String matricula) {
        Usuario usuario = service.buscarPorMatricula(matricula);
        return UsuarioDetalheResponse.from(usuario, service.listarAtribuicoes(usuario.getId()));
    }

    @PostMapping("/{id}/atribuicoes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USUARIO_ATRIBUIR_ACESSO')")
    public AtribuicaoResponse atribuir(
            @PathVariable UUID id,
            @Valid @RequestBody CriarAtribuicaoRequest request
    ) {
        return AtribuicaoResponse.from(service.atribuir(
                id,
                request.papel(),
                request.setorId(),
                request.turnoId(),
                request.inicioVigencia(),
                request.fimVigencia()
        ));
    }

    public record CriarUsuarioRequest(
            @NotBlank @Size(max = 80) String matricula,
            @NotBlank @Size(max = 160) String nome
    ) {
    }

    public record CriarAtribuicaoRequest(
            @NotNull Papel papel,
            UUID setorId,
            UUID turnoId,
            Instant inicioVigencia,
            Instant fimVigencia
    ) {
    }

    public record UsuarioResponse(UUID id, String matricula, String nome, boolean ativo, long versao) {
        static UsuarioResponse from(Usuario usuario) {
            return new UsuarioResponse(
                    usuario.getId(), usuario.getMatricula(), usuario.getNome(), usuario.isAtivo(), usuario.getVersao()
            );
        }
    }

    public record UsuarioDetalheResponse(
            UUID id,
            String matricula,
            String nome,
            boolean ativo,
            long versao,
            List<AtribuicaoResponse> atribuicoes
    ) {
        static UsuarioDetalheResponse from(Usuario usuario, List<AtribuicaoAcesso> atribuicoes) {
            return new UsuarioDetalheResponse(
                    usuario.getId(),
                    usuario.getMatricula(),
                    usuario.getNome(),
                    usuario.isAtivo(),
                    usuario.getVersao(),
                    atribuicoes.stream().map(AtribuicaoResponse::from).toList()
            );
        }
    }

    public record AtribuicaoResponse(
            UUID id,
            Papel papel,
            UUID setorId,
            String setor,
            UUID turnoId,
            String turno,
            Instant inicioVigencia,
            Instant fimVigencia,
            boolean ativo
    ) {
        static AtribuicaoResponse from(AtribuicaoAcesso atribuicao) {
            return new AtribuicaoResponse(
                    atribuicao.getId(),
                    atribuicao.getPapel(),
                    atribuicao.getSetor() == null ? null : atribuicao.getSetor().getId(),
                    atribuicao.getSetor() == null ? null : atribuicao.getSetor().getNome(),
                    atribuicao.getTurno() == null ? null : atribuicao.getTurno().getId(),
                    atribuicao.getTurno() == null ? null : atribuicao.getTurno().getNome(),
                    atribuicao.getInicioVigencia(),
                    atribuicao.getFimVigencia(),
                    atribuicao.isAtivo()
            );
        }
    }
}
