package com.gerenciamentoit.acesso.application;

import com.gerenciamentoit.acesso.domain.AtribuicaoAcesso;
import com.gerenciamentoit.acesso.domain.OrigemAplicacao;
import com.gerenciamentoit.acesso.domain.Papel;
import com.gerenciamentoit.acesso.domain.Permissao;
import com.gerenciamentoit.acesso.domain.SessaoAcesso;
import com.gerenciamentoit.acesso.domain.Usuario;
import com.gerenciamentoit.acesso.repository.AtribuicaoAcessoRepository;
import com.gerenciamentoit.acesso.repository.SessaoAcessoRepository;
import com.gerenciamentoit.acesso.repository.UsuarioRepository;
import com.gerenciamentoit.shared.config.AppProperties;
import com.gerenciamentoit.shared.error.ForbiddenException;
import com.gerenciamentoit.shared.error.NotFoundException;
import com.gerenciamentoit.shared.security.UsuarioAutenticado;
import com.gerenciamentoit.shared.util.Normalizer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class SessaoService {

    private final UsuarioRepository usuarioRepository;
    private final AtribuicaoAcessoRepository atribuicaoRepository;
    private final SessaoAcessoRepository sessaoRepository;
    private final TokenService tokenService;
    private final AppProperties.Security properties;

    public SessaoService(
            UsuarioRepository usuarioRepository,
            AtribuicaoAcessoRepository atribuicaoRepository,
            SessaoAcessoRepository sessaoRepository,
            TokenService tokenService,
            AppProperties.Security properties
    ) {
        this.usuarioRepository = usuarioRepository;
        this.atribuicaoRepository = atribuicaoRepository;
        this.sessaoRepository = sessaoRepository;
        this.tokenService = tokenService;
        this.properties = properties;
    }

    @Transactional
    public SessaoCriada criar(String matriculaInformada, OrigemAplicacao origem) {
        String matricula = Normalizer.codigoObrigatorio(matriculaInformada);
        Usuario usuario = usuarioRepository.findByMatricula(matricula)
                .orElseThrow(() -> new NotFoundException("MATRICULA_NAO_ENCONTRADA", "Matricula nao encontrada."));

        if (!usuario.isAtivo()) {
            throw new ForbiddenException("USUARIO_INATIVO", "O usuario esta inativo.");
        }

        Instant agora = Instant.now();
        String token = tokenService.gerar();
        SessaoAcesso sessao = new SessaoAcesso(
                usuario,
                tokenService.hash(token),
                origem == null ? OrigemAplicacao.API : origem,
                agora.plus(properties.sessionDuration())
        );
        sessaoRepository.save(sessao);

        UsuarioAutenticado principal = montarPrincipal(sessao, agora);
        return new SessaoCriada(token, sessao.getExpiraEm(), principal);
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioAutenticado> autenticar(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Instant agora = Instant.now();
        return sessaoRepository.findByTokenHashAndRevogadaEmIsNull(tokenService.hash(token.trim()))
                .filter(sessao -> sessao.estaValida(agora))
                .map(sessao -> montarPrincipal(sessao, agora));
    }

    @Transactional
    public void revogar(UUID sessaoId) {
        SessaoAcesso sessao = sessaoRepository.findById(sessaoId)
                .orElseThrow(() -> new NotFoundException("SESSAO_NAO_ENCONTRADA", "Sessao nao encontrada."));
        if (sessao.getRevogadaEm() == null) {
            sessao.revogar(Instant.now());
        }
    }

    @Scheduled(cron = "0 20 3 * * *")
    @Transactional
    public void limparSessoesExpiradas() {
        sessaoRepository.deleteByExpiraEmBefore(Instant.now().minusSeconds(86_400));
    }

    private UsuarioAutenticado montarPrincipal(SessaoAcesso sessao, Instant agora) {
        Usuario usuario = sessao.getUsuario();
        Set<Papel> papeis = EnumSet.noneOf(Papel.class);
        Set<Permissao> permissoes = EnumSet.noneOf(Permissao.class);
        Set<UUID> setores = new HashSet<>();
        Set<UUID> turnos = new HashSet<>();

        for (AtribuicaoAcesso atribuicao : atribuicaoRepository.findByUsuarioIdAndAtivoTrue(usuario.getId())) {
            if (!atribuicao.estaVigente(agora)) {
                continue;
            }
            papeis.add(atribuicao.getPapel());
            permissoes.addAll(atribuicao.getPapel().getPermissoes());
            if (atribuicao.getSetor() != null) {
                setores.add(atribuicao.getSetor().getId());
            }
            if (atribuicao.getTurno() != null) {
                turnos.add(atribuicao.getTurno().getId());
            }
        }

        return new UsuarioAutenticado(
                usuario.getId(),
                sessao.getId(),
                usuario.getMatricula(),
                usuario.getNome(),
                sessao.getOrigemAplicacao(),
                papeis,
                permissoes,
                setores,
                turnos
        );
    }

    public record SessaoCriada(String token, Instant expiraEm, UsuarioAutenticado usuario) {
    }
}
