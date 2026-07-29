package com.gerenciamentoit.conferencia.application;

import com.gerenciamentoit.acesso.domain.Usuario;
import com.gerenciamentoit.acesso.repository.UsuarioRepository;
import com.gerenciamentoit.ativos.domain.Ativo;
import com.gerenciamentoit.ativos.domain.SituacaoPatrimonial;
import com.gerenciamentoit.ativos.repository.AtivoRepository;
import com.gerenciamentoit.auditoria.application.AuditoriaService;
import com.gerenciamentoit.conferencia.domain.Conferencia;
import com.gerenciamentoit.conferencia.domain.ConferenciaItem;
import com.gerenciamentoit.conferencia.domain.LeituraConferencia;
import com.gerenciamentoit.conferencia.domain.ResultadoLeituraConferencia;
import com.gerenciamentoit.conferencia.domain.StatusConferencia;
import com.gerenciamentoit.conferencia.domain.StatusItemConferencia;
import com.gerenciamentoit.conferencia.domain.TipoConferencia;
import com.gerenciamentoit.conferencia.repository.ConferenciaItemRepository;
import com.gerenciamentoit.conferencia.repository.ConferenciaRepository;
import com.gerenciamentoit.conferencia.repository.LeituraConferenciaRepository;
import com.gerenciamentoit.organizacao.application.ContextoOperacionalService;
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
import java.util.UUID;

@Service
public class ConferenciaService {

    private final ConferenciaRepository conferenciaRepository;
    private final ConferenciaItemRepository itemRepository;
    private final LeituraConferenciaRepository leituraRepository;
    private final AtivoRepository ativoRepository;
    private final SetorRepository setorRepository;
    private final TurnoRepository turnoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContextoAutenticacao contextoAutenticacao;
    private final ContextoOperacionalService contextoOperacionalService;
    private final AuditoriaService auditoria;

    public ConferenciaService(
            ConferenciaRepository conferenciaRepository,
            ConferenciaItemRepository itemRepository,
            LeituraConferenciaRepository leituraRepository,
            AtivoRepository ativoRepository,
            SetorRepository setorRepository,
            TurnoRepository turnoRepository,
            UsuarioRepository usuarioRepository,
            ContextoAutenticacao contextoAutenticacao,
            ContextoOperacionalService contextoOperacionalService,
            AuditoriaService auditoria
    ) {
        this.conferenciaRepository = conferenciaRepository;
        this.itemRepository = itemRepository;
        this.leituraRepository = leituraRepository;
        this.ativoRepository = ativoRepository;
        this.setorRepository = setorRepository;
        this.turnoRepository = turnoRepository;
        this.usuarioRepository = usuarioRepository;
        this.contextoAutenticacao = contextoAutenticacao;
        this.contextoOperacionalService = contextoOperacionalService;
        this.auditoria = auditoria;
    }

    @Transactional
    public ConferenciaDetalhe abrir(AbrirConferencia comando) {
        validarContexto(comando.setorId(), comando.turnoId());

        var existente = conferenciaRepository
                .findFirstBySetorIdAndTurnoIdAndTipoAndStatusOrderByCriadoEmDesc(
                        comando.setorId(),
                        comando.turnoId(),
                        comando.tipo(),
                        StatusConferencia.EM_ANDAMENTO
                );
        if (existente.isPresent()) {
            return detalhe(existente.get());
        }

        Setor setor = setorRepository.findById(comando.setorId())
                .filter(Setor::isAtivo)
                .orElseThrow(() -> new NotFoundException(
                        "SETOR_NAO_ENCONTRADO",
                        "Setor nao encontrado ou inativo."
                ));
        Turno turno = turnoRepository.findById(comando.turnoId())
                .filter(Turno::isAtivo)
                .orElseThrow(() -> new NotFoundException(
                        "TURNO_NAO_ENCONTRADO",
                        "Turno nao encontrado ou inativo."
                ));
        UsuarioAutenticado principal = contextoAutenticacao.atual();
        Usuario responsavel = usuarioRepository.getReferenceById(principal.usuarioId());

        Conferencia conferencia = conferenciaRepository.save(
                new Conferencia(setor, turno, responsavel, comando.tipo())
        );
        List<Ativo> esperados = ativoRepository
                .findByTipoControlaPoolTrueAndSetorPermanenteIdAndSituacaoPatrimonialNotOrderByNumeroSerieAsc(
                        setor.getId(),
                        SituacaoPatrimonial.BAIXADO
                );
        itemRepository.saveAll(
                esperados.stream()
                        .map(ativo -> new ConferenciaItem(conferencia, ativo))
                        .toList()
        );

        auditoria.registrar(
                "CONFERENCIA_INICIADA",
                "CONFERENCIA",
                conferencia.getId(),
                null,
                snapshot(conferencia, esperados.size(), 0, 0),
                null
        );
        return detalhe(conferencia);
    }

    @Transactional(readOnly = true)
    public ConferenciaDetalhe buscar(UUID id) {
        return detalhe(buscarAutorizada(id));
    }

    @Transactional(readOnly = true)
    public List<ItemConferencia> listarItens(UUID id) {
        buscarAutorizada(id);
        return itemRepository.findByConferenciaIdOrderByAtivoNumeroSerieAsc(id).stream()
                .map(ItemConferencia::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeituraRegistrada> listarLeituras(UUID id) {
        buscarAutorizada(id);
        return leituraRepository.findByConferenciaIdOrderByCriadoEmDesc(id).stream()
                .map(LeituraRegistrada::from)
                .toList();
    }

    @Transactional
    public LeituraComResumo registrarLeitura(UUID id, String codigoInformado) {
        Conferencia conferencia = buscarAutorizada(id);
        exigirEmAndamento(conferencia);
        String codigo = Normalizer.codigoObrigatorio(codigoInformado);
        Instant agora = Instant.now();

        Ativo ativo = null;
        ResultadoLeituraConferencia resultado;
        List<Ativo> encontrados = ativoRepository.findByNumeroSerieOrPatrimonio(codigo, codigo);

        if (encontrados.size() > 1) {
            resultado = ResultadoLeituraConferencia.IDENTIFICADOR_AMBIGUO;
        } else if (encontrados.isEmpty()) {
            resultado = ResultadoLeituraConferencia.NAO_CADASTRADA;
        } else {
            ativo = encontrados.getFirst();
            resultado = classificar(conferencia, ativo, agora);
        }

        LeituraConferencia leitura = leituraRepository.save(
                new LeituraConferencia(conferencia, ativo, codigo, resultado)
        );
        return new LeituraComResumo(
                LeituraRegistrada.from(leitura),
                resumo(conferencia.getId())
        );
    }

    @Transactional
    public ConferenciaDetalhe concluir(UUID id) {
        Conferencia conferencia = buscarAutorizada(id);
        exigirEmAndamento(conferencia);

        List<ConferenciaItem> itens = itemRepository
                .findByConferenciaIdOrderByAtivoNumeroSerieAsc(id);
        itens.forEach(ConferenciaItem::marcarAusente);
        conferencia.concluir(Instant.now());
        conferenciaRepository.flush();

        ResumoConferencia resumo = resumo(id);
        auditoria.registrar(
                "CONFERENCIA_CONCLUIDA",
                "CONFERENCIA",
                conferencia.getId(),
                null,
                snapshot(
                        conferencia,
                        resumo.esperadas(),
                        resumo.confirmadas(),
                        resumo.ausentes()
                ),
                null
        );
        return ConferenciaDetalhe.from(conferencia, resumo);
    }

    private ResultadoLeituraConferencia classificar(
            Conferencia conferencia,
            Ativo ativo,
            Instant agora
    ) {
        if (leituraRepository.existsByConferenciaIdAndAtivoId(
                conferencia.getId(),
                ativo.getId()
        )) {
            return ResultadoLeituraConferencia.DUPLICADA;
        }
        if (ativo.getSituacaoPatrimonial() == SituacaoPatrimonial.BAIXADO) {
            return ResultadoLeituraConferencia.BAIXADA;
        }
        if (!ativo.getTipo().isControlaPool()) {
            return ResultadoLeituraConferencia.NAO_CONTROLADA;
        }

        var item = itemRepository.findByConferenciaIdAndAtivoId(
                conferencia.getId(),
                ativo.getId()
        );
        if (item.isPresent()) {
            item.get().confirmar(agora);
            return ResultadoLeituraConferencia.CONFIRMADA;
        }
        if (ativo.getSetorPermanente() == null
                || !ativo.getSetorPermanente().getId().equals(conferencia.getSetor().getId())) {
            return ResultadoLeituraConferencia.OUTRO_SETOR;
        }
        return ResultadoLeituraConferencia.EXTRA;
    }

    private Conferencia buscarAutorizada(UUID id) {
        Conferencia conferencia = conferenciaRepository.findWithContextById(id)
                .orElseThrow(() -> new NotFoundException(
                        "CONFERENCIA_NAO_ENCONTRADA",
                        "Conferencia nao encontrada."
                ));
        validarContexto(conferencia.getSetor().getId(), conferencia.getTurno().getId());
        return conferencia;
    }

    private void validarContexto(UUID setorId, UUID turnoId) {
        boolean permitido = contextoOperacionalService.listarDoUsuarioAtual().stream()
                .anyMatch(contexto -> contexto.setorId().equals(setorId)
                        && contexto.turnoId().equals(turnoId));
        if (!permitido) {
            throw new ForbiddenException(
                    "CONTEXTO_NAO_AUTORIZADO",
                    "O usuario nao pode operar neste setor e turno."
            );
        }
    }

    private void exigirEmAndamento(Conferencia conferencia) {
        if (conferencia.getStatus() != StatusConferencia.EM_ANDAMENTO) {
            throw new ConflictException(
                    "CONFERENCIA_JA_CONCLUIDA",
                    "A conferencia ja foi concluida e nao aceita novas operacoes."
            );
        }
    }

    private ConferenciaDetalhe detalhe(Conferencia conferencia) {
        return ConferenciaDetalhe.from(conferencia, resumo(conferencia.getId()));
    }

    private ResumoConferencia resumo(UUID conferenciaId) {
        List<ConferenciaItem> itens = itemRepository
                .findByConferenciaIdOrderByAtivoNumeroSerieAsc(conferenciaId);
        List<LeituraConferencia> leituras = leituraRepository
                .findByConferenciaIdOrderByCriadoEmDesc(conferenciaId);

        return new ResumoConferencia(
                itens.size(),
                leituras.size(),
                contarItens(itens, StatusItemConferencia.CONFIRMADA),
                contarItens(itens, StatusItemConferencia.PENDENTE),
                contarItens(itens, StatusItemConferencia.AUSENTE),
                contarLeituras(leituras, ResultadoLeituraConferencia.DUPLICADA),
                contarLeituras(leituras, ResultadoLeituraConferencia.EXTRA),
                contarLeituras(leituras, ResultadoLeituraConferencia.NAO_CADASTRADA),
                contarLeituras(leituras, ResultadoLeituraConferencia.OUTRO_SETOR),
                contarDivergencias(leituras)
        );
    }

    private int contarItens(List<ConferenciaItem> itens, StatusItemConferencia status) {
        return Math.toIntExact(itens.stream().filter(item -> item.getStatus() == status).count());
    }

    private int contarLeituras(
            List<LeituraConferencia> leituras,
            ResultadoLeituraConferencia resultado
    ) {
        return Math.toIntExact(
                leituras.stream().filter(leitura -> leitura.getResultado() == resultado).count()
        );
    }

    private int contarDivergencias(List<LeituraConferencia> leituras) {
        return Math.toIntExact(leituras.stream()
                .filter(leitura -> leitura.getResultado() != ResultadoLeituraConferencia.CONFIRMADA)
                .filter(leitura -> leitura.getResultado() != ResultadoLeituraConferencia.DUPLICADA)
                .count());
    }

    private String snapshot(
            Conferencia conferencia,
            int esperadas,
            int confirmadas,
            int ausentes
    ) {
        return "tipo=" + conferencia.getTipo()
                + ";status=" + conferencia.getStatus()
                + ";setor=" + conferencia.getSetor().getCodigo()
                + ";turno=" + conferencia.getTurno().getCodigo()
                + ";esperadas=" + esperadas
                + ";confirmadas=" + confirmadas
                + ";ausentes=" + ausentes;
    }

    public record AbrirConferencia(
            UUID setorId,
            UUID turnoId,
            TipoConferencia tipo
    ) {
        public AbrirConferencia {
            if (setorId == null) {
                throw new ValidationException(
                        "SETOR_OBRIGATORIO",
                        "O setor e obrigatorio.",
                        "setorId"
                );
            }
            if (turnoId == null) {
                throw new ValidationException(
                        "TURNO_OBRIGATORIO",
                        "O turno e obrigatorio.",
                        "turnoId"
                );
            }
            if (tipo == null) {
                throw new ValidationException(
                        "TIPO_CONFERENCIA_OBRIGATORIO",
                        "O tipo da conferencia e obrigatorio.",
                        "tipo"
                );
            }
        }
    }

    public record ResumoConferencia(
            int esperadas,
            int leituras,
            int confirmadas,
            int pendentes,
            int ausentes,
            int duplicadas,
            int extras,
            int naoCadastradas,
            int outroSetor,
            int divergencias
    ) {
    }

    public record ConferenciaDetalhe(
            UUID id,
            TipoConferencia tipo,
            StatusConferencia status,
            UUID setorId,
            String setorCodigo,
            String setorNome,
            UUID turnoId,
            String turnoCodigo,
            String turnoNome,
            UUID responsavelId,
            String responsavelMatricula,
            String responsavelNome,
            Instant iniciadaEm,
            Instant concluidaEm,
            ResumoConferencia resumo
    ) {
        static ConferenciaDetalhe from(
                Conferencia conferencia,
                ResumoConferencia resumo
        ) {
            return new ConferenciaDetalhe(
                    conferencia.getId(),
                    conferencia.getTipo(),
                    conferencia.getStatus(),
                    conferencia.getSetor().getId(),
                    conferencia.getSetor().getCodigo(),
                    conferencia.getSetor().getNome(),
                    conferencia.getTurno().getId(),
                    conferencia.getTurno().getCodigo(),
                    conferencia.getTurno().getNome(),
                    conferencia.getResponsavel().getId(),
                    conferencia.getResponsavel().getMatricula(),
                    conferencia.getResponsavel().getNome(),
                    conferencia.getCriadoEm(),
                    conferencia.getConcluidaEm(),
                    resumo
            );
        }
    }

    public record ItemConferencia(
            UUID id,
            UUID ativoId,
            String numeroSerie,
            String patrimonio,
            String tipoCodigo,
            String modelo,
            String disponibilidade,
            StatusItemConferencia status,
            Instant confirmadaEm
    ) {
        static ItemConferencia from(ConferenciaItem item) {
            Ativo ativo = item.getAtivo();
            return new ItemConferencia(
                    item.getId(),
                    ativo.getId(),
                    ativo.getNumeroSerie(),
                    ativo.getPatrimonio(),
                    ativo.getTipo().getCodigo(),
                    ativo.getModelo(),
                    ativo.getDisponibilidade().name(),
                    item.getStatus(),
                    item.getConfirmadaEm()
            );
        }
    }

    public record LeituraRegistrada(
            UUID id,
            String codigo,
            ResultadoLeituraConferencia resultado,
            UUID ativoId,
            String numeroSerie,
            String patrimonio,
            String setorCodigo,
            Instant lidaEm
    ) {
        static LeituraRegistrada from(LeituraConferencia leitura) {
            Ativo ativo = leitura.getAtivo();
            return new LeituraRegistrada(
                    leitura.getId(),
                    leitura.getCodigoLido(),
                    leitura.getResultado(),
                    ativo == null ? null : ativo.getId(),
                    ativo == null ? null : ativo.getNumeroSerie(),
                    ativo == null ? null : ativo.getPatrimonio(),
                    ativo == null || ativo.getSetorPermanente() == null
                            ? null
                            : ativo.getSetorPermanente().getCodigo(),
                    leitura.getCriadoEm()
            );
        }
    }

    public record LeituraComResumo(
            LeituraRegistrada leitura,
            ResumoConferencia resumo
    ) {
    }
}
