package com.gerenciamentoit.acesso.web;

import com.gerenciamentoit.acesso.application.SessaoService;
import com.gerenciamentoit.acesso.domain.OrigemAplicacao;
import com.gerenciamentoit.shared.security.ContextoAutenticacao;
import com.gerenciamentoit.shared.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
public class SessaoController {

    private final SessaoService sessaoService;
    private final ContextoAutenticacao contexto;

    public SessaoController(SessaoService sessaoService, ContextoAutenticacao contexto) {
        this.sessaoService = sessaoService;
        this.contexto = contexto;
    }

    @PostMapping("/sessoes")
    @ResponseStatus(HttpStatus.CREATED)
    public SessaoResponse criar(@Valid @RequestBody CriarSessaoRequest request) {
        SessaoService.SessaoCriada sessao = sessaoService.criar(request.matricula(), request.origemAplicacao());
        return new SessaoResponse(sessao.token(), sessao.expiraEm(), UsuarioResponse.from(sessao.usuario()));
    }

    @DeleteMapping("/sessoes/atual")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revogarAtual() {
        sessaoService.revogar(contexto.atual().sessaoId());
    }

    @GetMapping("/me")
    public UsuarioResponse me() {
        return UsuarioResponse.from(contexto.atual());
    }

    public record CriarSessaoRequest(
            @NotBlank @Size(max = 80) String matricula,
            OrigemAplicacao origemAplicacao
    ) {
    }

    public record SessaoResponse(String token, Instant expiraEm, UsuarioResponse usuario) {
    }

    public record UsuarioResponse(
            UUID id,
            String matricula,
            String nome,
            Set<String> papeis,
            Set<String> permissoes,
            Set<UUID> setores,
            Set<UUID> turnos,
            OrigemAplicacao origemAplicacao
    ) {
        static UsuarioResponse from(UsuarioAutenticado usuario) {
            return new UsuarioResponse(
                    usuario.usuarioId(),
                    usuario.matricula(),
                    usuario.nome(),
                    usuario.papeis().stream().map(Enum::name).collect(java.util.stream.Collectors.toUnmodifiableSet()),
                    usuario.permissoes().stream().map(Enum::name).collect(java.util.stream.Collectors.toUnmodifiableSet()),
                    usuario.setores(),
                    usuario.turnos(),
                    usuario.origemAplicacao()
            );
        }
    }
}
