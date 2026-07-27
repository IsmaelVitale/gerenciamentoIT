package com.gerenciamentoit.chamados.application;

import com.gerenciamentoit.acesso.domain.OrigemAplicacao;
import com.gerenciamentoit.acesso.domain.Usuario;
import com.gerenciamentoit.acesso.repository.UsuarioRepository;
import com.gerenciamentoit.auditoria.application.AuditoriaService;
import com.gerenciamentoit.chamados.domain.Chamado;
import com.gerenciamentoit.chamados.domain.StatusChamado;
import com.gerenciamentoit.chamados.repository.ChamadoRepository;
import com.gerenciamentoit.shared.error.ConflictException;
import com.gerenciamentoit.shared.error.NotFoundException;
import com.gerenciamentoit.shared.error.ValidationException;
import com.gerenciamentoit.shared.security.ContextoAutenticacao;
import com.gerenciamentoit.shared.security.UsuarioAutenticado;
import com.gerenciamentoit.shared.util.Normalizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
public class ChamadoService {

    private static final DateTimeFormatter DATA_PROTOCOLO = DateTimeFormatter.BASIC_ISO_DATE
            .withZone(ZoneOffset.UTC);

    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContextoAutenticacao contexto;
    private final AuditoriaService auditoria;

    public ChamadoService(
            ChamadoRepository chamadoRepository,
            UsuarioRepository usuarioRepository,
            ContextoAutenticacao contexto,
            AuditoriaService auditoria
    ) {
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.contexto = contexto;
        this.auditoria = auditoria;
    }

    @Transactional
    public AberturaChamado abrir(AbrirChamado comando) {
        UsuarioAutenticado principal = contexto.atual();
        OrigemAplicacao origem = principal.origemAplicacao() == null
                ? OrigemAplicacao.API
                : principal.origemAplicacao();
        String descricao = validarDescricao(comando.descricao());
        String telefone = normalizarTelefone(comando.telefoneContato());
        String identificadorExterno = validarIdentificadorExterno(comando.identificadorExterno());

        if (identificadorExterno != null) {
            var existente = chamadoRepository.findByOrigemAplicacaoAndIdentificadorExterno(
                    origem,
                    identificadorExterno
            );
            if (existente.isPresent()) {
                Chamado chamado = existente.get();
                if (!chamado.getSolicitante().getId().equals(principal.usuarioId())) {
                    throw new ConflictException(
                            "IDENTIFICADOR_EXTERNO_JA_UTILIZADO",
                            "O identificador externo informado ja pertence a outro chamado.",
                            "identificadorExterno"
                    );
                }
                return new AberturaChamado(ChamadoDetalhe.from(chamado), false);
            }
        }

        Usuario solicitante = usuarioRepository.getReferenceById(principal.usuarioId());
        Chamado chamado = chamadoRepository.saveAndFlush(new Chamado(
                gerarProtocolo(),
                solicitante,
                descricao,
                telefone,
                origem,
                identificadorExterno
        ));

        auditoria.registrar(
                "CHAMADO_ABERTO",
                "CHAMADO",
                chamado.getId(),
                null,
                snapshot(chamado),
                null
        );

        return new AberturaChamado(ChamadoDetalhe.from(chamado), true);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResumo> listarMeus(Pageable pageable) {
        UUID usuarioId = contexto.atual().usuarioId();
        return chamadoRepository.findBySolicitante_Id(usuarioId, pageable).map(ChamadoResumo::from);
    }

    @Transactional(readOnly = true)
    public ChamadoDetalhe buscarMeu(UUID id) {
        UUID usuarioId = contexto.atual().usuarioId();
        Chamado chamado = chamadoRepository.findByIdAndSolicitante_Id(id, usuarioId)
                .orElseThrow(() -> new NotFoundException(
                        "CHAMADO_NAO_ENCONTRADO",
                        "Chamado nao encontrado para o usuario autenticado."
                ));
        return ChamadoDetalhe.from(chamado);
    }

    @Transactional(readOnly = true)
    public ChamadoDetalhe buscarMeuPorProtocolo(String protocoloInformado) {
        UUID usuarioId = contexto.atual().usuarioId();
        String protocolo = Normalizer.codigoObrigatorio(protocoloInformado);
        Chamado chamado = chamadoRepository.findByProtocoloAndSolicitante_Id(protocolo, usuarioId)
                .orElseThrow(() -> new NotFoundException(
                        "CHAMADO_NAO_ENCONTRADO",
                        "Chamado nao encontrado para o usuario autenticado."
                ));
        return ChamadoDetalhe.from(chamado);
    }

    private String validarDescricao(String valor) {
        String descricao = Normalizer.textoObrigatorio(valor);
        if (descricao.length() > 4000) {
            throw new ValidationException(
                    "DESCRICAO_CHAMADO_MUITO_LONGA",
                    "A descricao do chamado deve possuir no maximo 4000 caracteres.",
                    "descricao"
            );
        }
        return descricao;
    }

    private String validarIdentificadorExterno(String valor) {
        String identificador = Normalizer.textoOpcional(valor);
        if (identificador != null && identificador.length() > 200) {
            throw new ValidationException(
                    "IDENTIFICADOR_EXTERNO_MUITO_LONGO",
                    "O identificador externo deve possuir no maximo 200 caracteres.",
                    "identificadorExterno"
            );
        }
        return identificador;
    }

    private String normalizarTelefone(String valor) {
        String telefoneInformado = Normalizer.textoOpcional(valor);
        if (telefoneInformado == null) {
            return null;
        }

        String telefone = telefoneInformado.replaceAll("\\D", "");
        if (telefone.length() < 8 || telefone.length() > 20) {
            throw new ValidationException(
                    "TELEFONE_CONTATO_INVALIDO",
                    "O telefone de contato deve possuir entre 8 e 20 digitos.",
                    "telefoneContato"
            );
        }
        return telefone;
    }

    private String gerarProtocolo() {
        String data = DATA_PROTOCOLO.format(Instant.now());
        String aleatorio = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase(Locale.ROOT);
        return "CH-" + data + "-" + aleatorio;
    }

    private String snapshot(Chamado chamado) {
        return "protocolo=" + chamado.getProtocolo()
                + ";status=" + chamado.getStatus()
                + ";origem=" + chamado.getOrigemAplicacao()
                + ";solicitanteId=" + chamado.getSolicitante().getId()
                + ";telefoneContatoInformado=" + (chamado.getTelefoneContato() != null);
    }

    public record AbrirChamado(
            String descricao,
            String telefoneContato,
            String identificadorExterno
    ) {
    }

    public record AberturaChamado(ChamadoDetalhe chamado, boolean criado) {
    }

    public record ChamadoResumo(
            UUID id,
            String protocolo,
            String descricao,
            StatusChamado status,
            OrigemAplicacao origemAplicacao,
            Instant criadoEm,
            Instant atualizadoEm
    ) {
        static ChamadoResumo from(Chamado chamado) {
            return new ChamadoResumo(
                    chamado.getId(),
                    chamado.getProtocolo(),
                    chamado.getDescricao(),
                    chamado.getStatus(),
                    chamado.getOrigemAplicacao(),
                    chamado.getCriadoEm(),
                    chamado.getAtualizadoEm()
            );
        }
    }

    public record ChamadoDetalhe(
            UUID id,
            String protocolo,
            UUID solicitanteId,
            String solicitanteMatricula,
            String solicitanteNome,
            String descricao,
            String telefoneContato,
            StatusChamado status,
            OrigemAplicacao origemAplicacao,
            String identificadorExterno,
            Instant criadoEm,
            Instant atualizadoEm,
            long versao
    ) {
        static ChamadoDetalhe from(Chamado chamado) {
            return new ChamadoDetalhe(
                    chamado.getId(),
                    chamado.getProtocolo(),
                    chamado.getSolicitante().getId(),
                    chamado.getSolicitante().getMatricula(),
                    chamado.getSolicitante().getNome(),
                    chamado.getDescricao(),
                    chamado.getTelefoneContato(),
                    chamado.getStatus(),
                    chamado.getOrigemAplicacao(),
                    chamado.getIdentificadorExterno(),
                    chamado.getCriadoEm(),
                    chamado.getAtualizadoEm(),
                    chamado.getVersao()
            );
        }
    }
}
