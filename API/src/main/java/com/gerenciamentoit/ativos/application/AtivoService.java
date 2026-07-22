package com.gerenciamentoit.ativos.application;

import com.gerenciamentoit.acesso.domain.Usuario;
import com.gerenciamentoit.acesso.repository.UsuarioRepository;
import com.gerenciamentoit.ativos.domain.Ativo;
import com.gerenciamentoit.ativos.domain.DisponibilidadeAtivo;
import com.gerenciamentoit.ativos.domain.LocalizacaoAtivo;
import com.gerenciamentoit.ativos.domain.MovimentacaoAtivo;
import com.gerenciamentoit.ativos.domain.SituacaoPatrimonial;
import com.gerenciamentoit.ativos.domain.TipoAtivo;
import com.gerenciamentoit.ativos.domain.TipoMovimentacaoAtivo;
import com.gerenciamentoit.ativos.repository.AtivoRepository;
import com.gerenciamentoit.ativos.repository.MovimentacaoAtivoRepository;
import com.gerenciamentoit.ativos.repository.TipoAtivoRepository;
import com.gerenciamentoit.auditoria.application.AuditoriaService;
import com.gerenciamentoit.shared.error.ConflictException;
import com.gerenciamentoit.shared.error.NotFoundException;
import com.gerenciamentoit.shared.error.ValidationException;
import com.gerenciamentoit.shared.security.ContextoAutenticacao;
import com.gerenciamentoit.shared.security.UsuarioAutenticado;
import com.gerenciamentoit.shared.util.Normalizer;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AtivoService {

    private final AtivoRepository ativoRepository;
    private final TipoAtivoRepository tipoRepository;
    private final MovimentacaoAtivoRepository movimentacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContextoAutenticacao contexto;
    private final AuditoriaService auditoria;

    public AtivoService(
            AtivoRepository ativoRepository,
            TipoAtivoRepository tipoRepository,
            MovimentacaoAtivoRepository movimentacaoRepository,
            UsuarioRepository usuarioRepository,
            ContextoAutenticacao contexto,
            AuditoriaService auditoria
    ) {
        this.ativoRepository = ativoRepository;
        this.tipoRepository = tipoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.contexto = contexto;
        this.auditoria = auditoria;
    }

    @Transactional
    public AtivoDetalhe cadastrar(CadastrarAtivo comando) {
        String numeroSerie = Normalizer.codigoObrigatorio(comando.numeroSerie());
        String patrimonio = Normalizer.codigoOpcional(comando.patrimonio());
        validarUnicidade(null, numeroSerie, patrimonio);

        TipoAtivo tipo = tipoRepository.findById(comando.tipoAtivoId())
                .filter(TipoAtivo::isAtivo)
                .orElseThrow(() -> new NotFoundException("TIPO_ATIVO_NAO_ENCONTRADO", "Tipo de ativo nao encontrado ou inativo."));

        Ativo ativo = ativoRepository.save(new Ativo(
                tipo,
                numeroSerie,
                patrimonio,
                Normalizer.textoOpcional(comando.fabricante()),
                Normalizer.textoOpcional(comando.modelo()),
                Normalizer.textoOpcional(comando.observacao())
        ));

        registrarMovimentacao(
                ativo,
                TipoMovimentacaoAtivo.CADASTRO,
                "Ativo cadastrado em preparacao. Numero de serie: " + numeroSerie
                        + (patrimonio == null ? "; sem patrimonio" : "; patrimonio: " + patrimonio)
        );
        auditoria.registrar("ATIVO_CADASTRADO", "ATIVO", ativo.getId(), null, snapshot(ativo), null);
        ativoRepository.flush();
        return AtivoDetalhe.from(ativo);
    }

    @Transactional
    public AtivoDetalhe liberar(UUID id) {
        Ativo ativo = buscarEntidade(id);
        if (ativo.getSituacaoPatrimonial() != SituacaoPatrimonial.EM_PREPARACAO) {
            throw new ConflictException(
                    "ATIVO_NAO_ESTA_EM_PREPARACAO",
                    "Somente ativos em preparacao podem ser liberados por esta operacao."
            );
        }

        String anterior = snapshot(ativo);
        ativo.liberar();
        registrarMovimentacao(ativo, TipoMovimentacaoAtivo.LIBERACAO, "Ativo liberado para uso pela T.I.");
        auditoria.registrar("ATIVO_LIBERADO", "ATIVO", ativo.getId(), anterior, snapshot(ativo), null);
        ativoRepository.flush();
        return AtivoDetalhe.from(ativo);
    }

    @Transactional
    public AtivoDetalhe corrigirIdentificacao(UUID id, CorrigirIdentificacao comando) {
        Ativo ativo = buscarEntidade(id);
        String numeroSerie = Normalizer.codigoObrigatorio(comando.numeroSerie());
        String patrimonio = comando.removerPatrimonio()
                ? null
                : Normalizer.codigoOpcional(comando.patrimonio());
        String motivo = Normalizer.textoObrigatorio(comando.motivo());
        validarUnicidade(id, numeroSerie, patrimonio);

        String anterior = snapshot(ativo);
        ativo.corrigirIdentificacao(numeroSerie, patrimonio);
        registrarMovimentacao(
                ativo,
                TipoMovimentacaoAtivo.CORRECAO_IDENTIFICACAO,
                "Identificacao corrigida. Motivo: " + motivo
        );
        auditoria.registrar(
                "IDENTIFICACAO_ATIVO_CORRIGIDA",
                "ATIVO",
                ativo.getId(),
                anterior,
                snapshot(ativo),
                motivo
        );
        ativoRepository.flush();
        return AtivoDetalhe.from(ativo);
    }

    @Transactional(readOnly = true)
    public AtivoDetalhe buscar(UUID id) {
        return AtivoDetalhe.from(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<AtivoResumo> pesquisar(FiltroAtivo filtro, Pageable pageable) {
        Specification<Ativo> spec = (root, query, cb) -> cb.conjunction();

        String numeroSerie = Normalizer.codigoOpcional(filtro.numeroSerie());
        String patrimonio = Normalizer.codigoOpcional(filtro.patrimonio());
        String tipoCodigo = Normalizer.codigoOpcional(filtro.tipoCodigo());

        if (numeroSerie != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("numeroSerie"), numeroSerie));
        }
        if (patrimonio != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("patrimonio"), patrimonio));
        }
        if (tipoCodigo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("tipo").get("codigo"), tipoCodigo));
        }
        if (filtro.situacaoPatrimonial() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("situacaoPatrimonial"), filtro.situacaoPatrimonial()));
        }
        if (filtro.disponibilidade() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("disponibilidade"), filtro.disponibilidade()));
        }

        return ativoRepository.findAll(spec, pageable).map(AtivoResumo::from);
    }

    private Ativo buscarEntidade(UUID id) {
        return ativoRepository.findWithTipoById(id)
                .orElseThrow(() -> new NotFoundException("ATIVO_NAO_ENCONTRADO", "Ativo nao encontrado."));
    }

    private void validarUnicidade(UUID idAtual, String numeroSerie, String patrimonio) {
        boolean numeroDuplicado = idAtual == null
                ? ativoRepository.existsByNumeroSerie(numeroSerie)
                : ativoRepository.existsByNumeroSerieAndIdNot(numeroSerie, idAtual);
        if (numeroDuplicado) {
            throw new ConflictException(
                    "NUMERO_SERIE_JA_CADASTRADO",
                    "Ja existe um ativo cadastrado com este numero de serie.",
                    "numeroSerie"
            );
        }

        if (patrimonio != null) {
            boolean patrimonioDuplicado = idAtual == null
                    ? ativoRepository.existsByPatrimonio(patrimonio)
                    : ativoRepository.existsByPatrimonioAndIdNot(patrimonio, idAtual);
            if (patrimonioDuplicado) {
                throw new ConflictException(
                        "PATRIMONIO_JA_CADASTRADO",
                        "Ja existe um ativo cadastrado com este patrimonio.",
                        "patrimonio"
                );
            }
        }
    }

    private void registrarMovimentacao(Ativo ativo, TipoMovimentacaoAtivo tipo, String descricao) {
        UsuarioAutenticado principal = contexto.atual();
        Usuario usuario = usuarioRepository.getReferenceById(principal.usuarioId());
        movimentacaoRepository.save(new MovimentacaoAtivo(
                ativo,
                usuario,
                tipo,
                principal.origemAplicacao(),
                descricao,
                MDC.get("correlationId")
        ));
    }

    private String snapshot(Ativo ativo) {
        return "numeroSerie=" + ativo.getNumeroSerie()
                + ";patrimonio=" + ativo.getPatrimonio()
                + ";tipo=" + ativo.getTipo().getCodigo()
                + ";situacao=" + ativo.getSituacaoPatrimonial()
                + ";disponibilidade=" + ativo.getDisponibilidade()
                + ";localizacao=" + ativo.getLocalizacaoAtual();
    }

    public record CadastrarAtivo(
            UUID tipoAtivoId,
            String numeroSerie,
            String patrimonio,
            String fabricante,
            String modelo,
            String observacao
    ) {
        public CadastrarAtivo {
            if (tipoAtivoId == null) {
                throw new ValidationException("TIPO_ATIVO_OBRIGATORIO", "O tipo do ativo e obrigatorio.", "tipoAtivoId");
            }
        }
    }

    public record CorrigirIdentificacao(
            String numeroSerie,
            String patrimonio,
            boolean removerPatrimonio,
            String motivo
    ) {
    }

    public record FiltroAtivo(
            String numeroSerie,
            String patrimonio,
            String tipoCodigo,
            SituacaoPatrimonial situacaoPatrimonial,
            DisponibilidadeAtivo disponibilidade
    ) {
    }

    public record AtivoResumo(
            UUID id,
            String tipoCodigo,
            String tipoNome,
            String numeroSerie,
            String patrimonio,
            String fabricante,
            String modelo,
            SituacaoPatrimonial situacaoPatrimonial,
            DisponibilidadeAtivo disponibilidade,
            LocalizacaoAtivo localizacaoAtual,
            Instant criadoEm,
            long versao
    ) {
        static AtivoResumo from(Ativo ativo) {
            return new AtivoResumo(
                    ativo.getId(),
                    ativo.getTipo().getCodigo(),
                    ativo.getTipo().getNome(),
                    ativo.getNumeroSerie(),
                    ativo.getPatrimonio(),
                    ativo.getFabricante(),
                    ativo.getModelo(),
                    ativo.getSituacaoPatrimonial(),
                    ativo.getDisponibilidade(),
                    ativo.getLocalizacaoAtual(),
                    ativo.getCriadoEm(),
                    ativo.getVersao()
            );
        }
    }

    public record AtivoDetalhe(
            UUID id,
            UUID tipoAtivoId,
            String tipoCodigo,
            String tipoNome,
            boolean tipoControlaPool,
            String numeroSerie,
            String patrimonio,
            String fabricante,
            String modelo,
            String observacao,
            SituacaoPatrimonial situacaoPatrimonial,
            DisponibilidadeAtivo disponibilidade,
            LocalizacaoAtivo localizacaoAtual,
            Instant criadoEm,
            Instant atualizadoEm,
            long versao
    ) {
        static AtivoDetalhe from(Ativo ativo) {
            return new AtivoDetalhe(
                    ativo.getId(),
                    ativo.getTipo().getId(),
                    ativo.getTipo().getCodigo(),
                    ativo.getTipo().getNome(),
                    ativo.getTipo().isControlaPool(),
                    ativo.getNumeroSerie(),
                    ativo.getPatrimonio(),
                    ativo.getFabricante(),
                    ativo.getModelo(),
                    ativo.getObservacao(),
                    ativo.getSituacaoPatrimonial(),
                    ativo.getDisponibilidade(),
                    ativo.getLocalizacaoAtual(),
                    ativo.getCriadoEm(),
                    ativo.getAtualizadoEm(),
                    ativo.getVersao()
            );
        }
    }
}
