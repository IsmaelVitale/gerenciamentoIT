package com.gerenciamentoit.acesso.bootstrap;

import com.gerenciamentoit.acesso.domain.AtribuicaoAcesso;
import com.gerenciamentoit.acesso.domain.Papel;
import com.gerenciamentoit.acesso.domain.Usuario;
import com.gerenciamentoit.acesso.repository.AtribuicaoAcessoRepository;
import com.gerenciamentoit.acesso.repository.UsuarioRepository;
import com.gerenciamentoit.ativos.domain.TipoAtivo;
import com.gerenciamentoit.ativos.repository.TipoAtivoRepository;
import com.gerenciamentoit.auditoria.application.AuditoriaService;
import com.gerenciamentoit.shared.config.AppProperties;
import com.gerenciamentoit.shared.util.Normalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class BootstrapInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapInicial.class);

    private final AppProperties.Bootstrap properties;
    private final UsuarioRepository usuarioRepository;
    private final AtribuicaoAcessoRepository atribuicaoRepository;
    private final TipoAtivoRepository tipoAtivoRepository;
    private final AuditoriaService auditoria;

    public BootstrapInicial(
            AppProperties.Bootstrap properties,
            UsuarioRepository usuarioRepository,
            AtribuicaoAcessoRepository atribuicaoRepository,
            TipoAtivoRepository tipoAtivoRepository,
            AuditoriaService auditoria
    ) {
        this.properties = properties;
        this.usuarioRepository = usuarioRepository;
        this.atribuicaoRepository = atribuicaoRepository;
        this.tipoAtivoRepository = tipoAtivoRepository;
        this.auditoria = auditoria;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            log.info("Bootstrap inicial desabilitado.");
            return;
        }

        criarTiposPadrao();
        criarGestorInicial();
    }

    private void criarTiposPadrao() {
        List<TipoPadrao> tipos = List.of(
                new TipoPadrao("PDA", "PDA / Coletor de dados", true),
                new TipoPadrao("NOTEBOOK", "Notebook", false),
                new TipoPadrao("CELULAR", "Celular corporativo", false),
                new TipoPadrao("IMPRESSORA", "Impressora", false),
                new TipoPadrao("PERIFERICO", "Periferico", false)
        );

        for (TipoPadrao padrao : tipos) {
            if (tipoAtivoRepository.existsByCodigo(padrao.codigo())) {
                continue;
            }
            TipoAtivo tipo = tipoAtivoRepository.save(new TipoAtivo(
                    padrao.codigo(), padrao.nome(), padrao.controlaPool()
            ));
            auditoria.registrarBootstrap(
                    "TIPO_ATIVO_CRIADO_NO_BOOTSTRAP",
                    "TIPO_ATIVO",
                    tipo.getId(),
                    "codigo=" + tipo.getCodigo() + ";nome=" + tipo.getNome()
            );
        }
    }

    private void criarGestorInicial() {
        String matricula = Normalizer.codigoObrigatorio(properties.adminMatricula());
        String nome = Normalizer.textoObrigatorio(properties.adminNome());

        Usuario usuario = usuarioRepository.findByMatricula(matricula).orElseGet(() -> {
            Usuario novo = usuarioRepository.save(new Usuario(matricula, nome));
            auditoria.registrarBootstrap(
                    "USUARIO_ADMIN_CRIADO_NO_BOOTSTRAP",
                    "USUARIO",
                    novo.getId(),
                    "matricula=" + novo.getMatricula() + ";nome=" + novo.getNome()
            );
            return novo;
        });

        boolean jaGestor = atribuicaoRepository.findByUsuarioIdAndAtivoTrue(usuario.getId()).stream()
                .anyMatch(atribuicao -> atribuicao.getPapel() == Papel.GESTOR_TI && atribuicao.estaVigente(Instant.now()));

        if (!jaGestor) {
            AtribuicaoAcesso atribuicao = atribuicaoRepository.save(new AtribuicaoAcesso(
                    usuario,
                    Papel.GESTOR_TI,
                    null,
                    null,
                    Instant.now(),
                    null
            ));
            auditoria.registrarBootstrap(
                    "PAPEL_GESTOR_CONCEDIDO_NO_BOOTSTRAP",
                    "ATRIBUICAO_ACESSO",
                    atribuicao.getId(),
                    "usuarioId=" + usuario.getId() + ";papel=GESTOR_TI"
            );
        }

        if ("ADMIN-LOCAL".equals(matricula)) {
            log.warn("A API esta usando a matricula administrativa padrao ADMIN-LOCAL. Defina BOOTSTRAP_ADMIN_MATRICULA antes de publicar o ambiente.");
        }
        log.info("Gestor inicial disponivel para autenticacao pela matricula {}.", matricula);
    }

    private record TipoPadrao(String codigo, String nome, boolean controlaPool) {
    }
}
