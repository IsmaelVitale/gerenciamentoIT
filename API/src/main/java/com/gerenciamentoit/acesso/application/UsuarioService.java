package com.gerenciamentoit.acesso.application;

import com.gerenciamentoit.acesso.domain.AtribuicaoAcesso;
import com.gerenciamentoit.acesso.domain.Papel;
import com.gerenciamentoit.acesso.domain.Usuario;
import com.gerenciamentoit.acesso.repository.AtribuicaoAcessoRepository;
import com.gerenciamentoit.acesso.repository.UsuarioRepository;
import com.gerenciamentoit.auditoria.application.AuditoriaService;
import com.gerenciamentoit.organizacao.domain.Setor;
import com.gerenciamentoit.organizacao.domain.Turno;
import com.gerenciamentoit.organizacao.repository.SetorRepository;
import com.gerenciamentoit.organizacao.repository.TurnoRepository;
import com.gerenciamentoit.shared.error.ConflictException;
import com.gerenciamentoit.shared.error.ForbiddenException;
import com.gerenciamentoit.shared.error.NotFoundException;
import com.gerenciamentoit.shared.error.ValidationException;
import com.gerenciamentoit.shared.security.ContextoAutenticacao;
import com.gerenciamentoit.shared.security.UsuarioAutenticado;
import com.gerenciamentoit.shared.util.Normalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AtribuicaoAcessoRepository atribuicaoRepository;
    private final SetorRepository setorRepository;
    private final TurnoRepository turnoRepository;
    private final ContextoAutenticacao contexto;
    private final AuditoriaService auditoria;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            AtribuicaoAcessoRepository atribuicaoRepository,
            SetorRepository setorRepository,
            TurnoRepository turnoRepository,
            ContextoAutenticacao contexto,
            AuditoriaService auditoria
    ) {
        this.usuarioRepository = usuarioRepository;
        this.atribuicaoRepository = atribuicaoRepository;
        this.setorRepository = setorRepository;
        this.turnoRepository = turnoRepository;
        this.contexto = contexto;
        this.auditoria = auditoria;
    }

    @Transactional
    public Usuario criar(String matriculaInformada, String nomeInformado) {
        String matricula = Normalizer.codigoObrigatorio(matriculaInformada);
        String nome = Normalizer.textoObrigatorio(nomeInformado);
        if (usuarioRepository.existsByMatricula(matricula)) {
            throw new ConflictException("MATRICULA_JA_CADASTRADA", "Ja existe um usuario com esta matricula.", "matricula");
        }

        Usuario usuario = usuarioRepository.save(new Usuario(matricula, nome));
        auditoria.registrar(
                "USUARIO_CRIADO",
                "USUARIO",
                usuario.getId(),
                null,
                "matricula=" + usuario.getMatricula() + ";nome=" + usuario.getNome(),
                null
        );
        return usuario;
    }

    @Transactional
    public AtribuicaoAcesso atribuir(
            UUID usuarioId,
            Papel papel,
            UUID setorId,
            UUID turnoId,
            Instant inicio,
            Instant fim
    ) {
        UsuarioAutenticado autor = contexto.atual();
        validarConcessao(autor, papel, setorId);
        validarEscopoDoPapel(papel, setorId, turnoId);

        Usuario usuario = buscarEntidade(usuarioId);
        Setor setor = setorId == null ? null : setorRepository.findById(setorId)
                .orElseThrow(() -> new NotFoundException("SETOR_NAO_ENCONTRADO", "Setor nao encontrado."));
        Turno turno = turnoId == null ? null : turnoRepository.findById(turnoId)
                .orElseThrow(() -> new NotFoundException("TURNO_NAO_ENCONTRADO", "Turno nao encontrado."));

        Instant inicioEfetivo = inicio == null ? Instant.now() : inicio;
        if (fim != null && !fim.isAfter(inicioEfetivo)) {
            throw new ValidationException("VIGENCIA_INVALIDA", "O fim da vigencia deve ser posterior ao inicio.", "fimVigencia");
        }

        boolean conflito = atribuicaoRepository.findByUsuarioIdAndAtivoTrue(usuarioId).stream()
                .filter(AtribuicaoAcesso::isAtivo)
                .filter(existente -> existente.getPapel() == papel)
                .filter(existente -> mesmoId(existente.getSetor(), setorId))
                .filter(existente -> mesmoId(existente.getTurno(), turnoId))
                .anyMatch(existente -> periodosSobrepostos(
                        existente.getInicioVigencia(),
                        existente.getFimVigencia(),
                        inicioEfetivo,
                        fim
                ));
        if (conflito) {
            throw new ConflictException(
                    "ATRIBUICAO_JA_EXISTENTE",
                    "O usuario ja possui uma atribuicao sobreposta para este papel e escopo."
            );
        }

        AtribuicaoAcesso atribuicao = atribuicaoRepository.save(new AtribuicaoAcesso(
                usuario, papel, setor, turno, inicioEfetivo, fim
        ));
        auditoria.registrar(
                "ACESSO_ATRIBUIDO",
                "ATRIBUICAO_ACESSO",
                atribuicao.getId(),
                null,
                "usuarioId=" + usuarioId + ";papel=" + papel
                        + ";setorId=" + setorId + ";turnoId=" + turnoId,
                null
        );
        return atribuicao;
    }

    @Transactional(readOnly = true)
    public Usuario buscar(UUID id) {
        Usuario usuario = buscarEntidade(id);
        validarVisualizacao(usuario);
        return usuario;
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorMatricula(String matriculaInformada) {
        String matricula = Normalizer.codigoObrigatorio(matriculaInformada);
        Usuario usuario = usuarioRepository.findByMatricula(matricula)
                .orElseThrow(() -> new NotFoundException("USUARIO_NAO_ENCONTRADO", "Usuario nao encontrado."));
        validarVisualizacao(usuario);
        return usuario;
    }

    @Transactional(readOnly = true)
    public List<AtribuicaoAcesso> listarAtribuicoes(UUID usuarioId) {
        Usuario usuario = buscarEntidade(usuarioId);
        validarVisualizacao(usuario);
        return atribuicaoRepository.findByUsuarioIdAndAtivoTrue(usuarioId);
    }

    private Usuario buscarEntidade(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("USUARIO_NAO_ENCONTRADO", "Usuario nao encontrado."));
    }

    private boolean mesmoId(Object entidade, UUID idEsperado) {
        if (entidade == null || idEsperado == null) {
            return entidade == null && idEsperado == null;
        }
        if (entidade instanceof Setor setor) {
            return setor.getId().equals(idEsperado);
        }
        if (entidade instanceof Turno turno) {
            return turno.getId().equals(idEsperado);
        }
        return false;
    }

    private boolean periodosSobrepostos(
            Instant inicioA,
            Instant fimA,
            Instant inicioB,
            Instant fimB
    ) {
        boolean aTerminaDepoisDoInicioB = fimA == null || fimA.isAfter(inicioB);
        boolean bTerminaDepoisDoInicioA = fimB == null || fimB.isAfter(inicioA);
        return aTerminaDepoisDoInicioB && bTerminaDepoisDoInicioA;
    }

    private void validarConcessao(UsuarioAutenticado autor, Papel destino, UUID setorId) {
        boolean pode = autor.papeis().stream().anyMatch(papel -> papel.podeConceder(destino));
        if (!pode) {
            throw new ForbiddenException("PAPEL_NAO_PODE_SER_CONCEDIDO", "Seu perfil nao pode conceder o papel solicitado.");
        }
        if (autor.possuiPapel(Papel.LIDER)
                && !autor.possuiPapel(Papel.SUPERVISOR)
                && !autor.possuiPapel(Papel.GESTOR_TI)
                && !autor.possuiSetor(setorId)) {
            throw new ForbiddenException("SETOR_FORA_DO_ESCOPO", "O setor informado nao pertence ao escopo do lider.");
        }
    }

    private void validarEscopoDoPapel(Papel papel, UUID setorId, UUID turnoId) {
        switch (papel) {
            case USUARIO -> {
                if (setorId == null) {
                    throw new ValidationException("SETOR_OBRIGATORIO", "Usuarios comuns devem possuir um setor.", "setorId");
                }
            }
            case LIDER -> {
                if (setorId == null || turnoId == null) {
                    throw new ValidationException("ESCOPO_LIDER_INCOMPLETO", "Lideres devem possuir setor e turno.");
                }
            }
            case SUPERVISOR, ANALISTA_TI, GESTOR_TI -> {
                if (setorId != null || turnoId != null) {
                    throw new ValidationException("PAPEL_GLOBAL", "Este papel deve ser concedido sem setor e sem turno.");
                }
            }
        }
    }

    private void validarVisualizacao(Usuario alvo) {
        UsuarioAutenticado autor = contexto.atual();
        if (autor.usuarioId().equals(alvo.getId())
                || autor.possuiPapel(Papel.GESTOR_TI)
                || autor.possuiPapel(Papel.SUPERVISOR)
                || autor.possuiPapel(Papel.ANALISTA_TI)) {
            return;
        }

        if (autor.possuiPapel(Papel.LIDER)) {
            Set<UUID> setoresDoAutor = autor.setores();
            boolean mesmoSetor = atribuicaoRepository.findByUsuarioIdAndAtivoTrue(alvo.getId()).stream()
                    .map(AtribuicaoAcesso::getSetor)
                    .filter(java.util.Objects::nonNull)
                    .map(Setor::getId)
                    .anyMatch(setoresDoAutor::contains);
            if (mesmoSetor) {
                return;
            }
        }

        throw new ForbiddenException("USUARIO_FORA_DO_ESCOPO", "O usuario solicitado esta fora do seu escopo.");
    }
}
