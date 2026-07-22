package com.gerenciamentoit.auditoria.application;

import com.gerenciamentoit.acesso.domain.OrigemAplicacao;
import com.gerenciamentoit.acesso.domain.Usuario;
import com.gerenciamentoit.acesso.repository.UsuarioRepository;
import com.gerenciamentoit.auditoria.domain.RegistroAuditoria;
import com.gerenciamentoit.auditoria.repository.RegistroAuditoriaRepository;
import com.gerenciamentoit.shared.security.ContextoAutenticacao;
import com.gerenciamentoit.shared.security.UsuarioAutenticado;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditoriaService {

    private final RegistroAuditoriaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final ContextoAutenticacao contextoAutenticacao;

    public AuditoriaService(
            RegistroAuditoriaRepository repository,
            UsuarioRepository usuarioRepository,
            ContextoAutenticacao contextoAutenticacao
    ) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.contextoAutenticacao = contextoAutenticacao;
    }

    public void registrar(
            String acao,
            String recurso,
            UUID recursoId,
            String anterior,
            String novo,
            String justificativa
    ) {
        UsuarioAutenticado principal = contextoAutenticacao.atual();
        Usuario usuario = usuarioRepository.getReferenceById(principal.usuarioId());
        salvar(usuario, acao, recurso, recursoId, principal.origemAplicacao(), anterior, novo, justificativa);
    }

    public void registrarBootstrap(
            String acao,
            String recurso,
            UUID recursoId,
            String novo
    ) {
        salvar(null, acao, recurso, recursoId, OrigemAplicacao.API, null, novo, "Inicializacao automatica");
    }

    private void salvar(
            Usuario usuario,
            String acao,
            String recurso,
            UUID recursoId,
            OrigemAplicacao origem,
            String anterior,
            String novo,
            String justificativa
    ) {
        repository.save(new RegistroAuditoria(
                usuario,
                acao,
                recurso,
                recursoId == null ? null : recursoId.toString(),
                origem,
                MDC.get("correlationId"),
                anterior,
                novo,
                justificativa
        ));
    }
}
