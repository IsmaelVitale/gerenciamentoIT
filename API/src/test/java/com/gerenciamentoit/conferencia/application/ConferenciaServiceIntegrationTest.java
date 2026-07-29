package com.gerenciamentoit.conferencia.application;

import com.gerenciamentoit.acesso.application.SessaoService;
import com.gerenciamentoit.acesso.domain.OrigemAplicacao;
import com.gerenciamentoit.ativos.application.AtivoService;
import com.gerenciamentoit.ativos.repository.TipoAtivoRepository;
import com.gerenciamentoit.conferencia.domain.ResultadoLeituraConferencia;
import com.gerenciamentoit.conferencia.domain.StatusConferencia;
import com.gerenciamentoit.conferencia.domain.StatusItemConferencia;
import com.gerenciamentoit.conferencia.domain.TipoConferencia;
import com.gerenciamentoit.organizacao.application.SetorService;
import com.gerenciamentoit.organizacao.application.TurnoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ConferenciaServiceIntegrationTest {

    @Autowired
    private ConferenciaService conferenciaService;

    @Autowired
    private AtivoService ativoService;

    @Autowired
    private TipoAtivoRepository tipoAtivoRepository;

    @Autowired
    private SetorService setorService;

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private SessaoService sessaoService;

    @BeforeEach
    void autenticarGestorDoBootstrap() {
        SessaoService.SessaoCriada sessao = sessaoService.criar(
                "ADMIN-TESTE",
                OrigemAplicacao.HUB_PDA
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(sessao.usuario(), sessao.token(), List.of())
        );
    }

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void conferePoolOnlineSemContarLeituraDuplicada() {
        var recebimento = setorService.criar("REC-CONF", "Recebimento Conferencia", 2);
        var expedicao = setorService.criar("EXP-CONF", "Expedicao Conferencia", 1);
        var turno = turnoService.criar(
                "T-CONF",
                "Turno Conferencia",
                LocalTime.of(6, 0),
                LocalTime.of(14, 0)
        );

        var pdaUm = criarLiberarEAlocar("SN-CONF-001", "PDA-CONF-001", recebimento.getId());
        criarLiberarEAlocar("SN-CONF-002", "PDA-CONF-002", recebimento.getId());
        var pdaOutroSetor = criarLiberarEAlocar(
                "SN-CONF-003",
                "PDA-CONF-003",
                expedicao.getId()
        );

        var aberta = conferenciaService.abrir(new ConferenciaService.AbrirConferencia(
                recebimento.getId(),
                turno.getId(),
                TipoConferencia.ABERTURA
        ));

        assertThat(aberta.status()).isEqualTo(StatusConferencia.EM_ANDAMENTO);
        assertThat(aberta.resumo().esperadas()).isEqualTo(2);

        var confirmada = conferenciaService.registrarLeitura(aberta.id(), pdaUm.numeroSerie());
        var duplicada = conferenciaService.registrarLeitura(aberta.id(), pdaUm.patrimonio());
        var desconhecida = conferenciaService.registrarLeitura(aberta.id(), "PDA-INEXISTENTE");
        var outroSetor = conferenciaService.registrarLeitura(
                aberta.id(),
                pdaOutroSetor.numeroSerie()
        );

        assertThat(confirmada.leitura().resultado())
                .isEqualTo(ResultadoLeituraConferencia.CONFIRMADA);
        assertThat(duplicada.leitura().resultado())
                .isEqualTo(ResultadoLeituraConferencia.DUPLICADA);
        assertThat(desconhecida.leitura().resultado())
                .isEqualTo(ResultadoLeituraConferencia.NAO_CADASTRADA);
        assertThat(outroSetor.leitura().resultado())
                .isEqualTo(ResultadoLeituraConferencia.OUTRO_SETOR);

        var concluida = conferenciaService.concluir(aberta.id());

        assertThat(concluida.status()).isEqualTo(StatusConferencia.CONCLUIDA);
        assertThat(concluida.resumo().confirmadas()).isEqualTo(1);
        assertThat(concluida.resumo().ausentes()).isEqualTo(1);
        assertThat(concluida.resumo().pendentes()).isZero();
        assertThat(concluida.resumo().duplicadas()).isEqualTo(1);
        assertThat(concluida.resumo().naoCadastradas()).isEqualTo(1);
        assertThat(concluida.resumo().outroSetor()).isEqualTo(1);
        assertThat(concluida.resumo().divergencias()).isEqualTo(2);

        assertThat(conferenciaService.listarItens(aberta.id()))
                .extracting(ConferenciaService.ItemConferencia::status)
                .containsExactlyInAnyOrder(
                        StatusItemConferencia.CONFIRMADA,
                        StatusItemConferencia.AUSENTE
                );
    }

    private AtivoService.AtivoDetalhe criarLiberarEAlocar(
            String numeroSerie,
            String patrimonio,
            java.util.UUID setorId
    ) {
        var tipoPda = tipoAtivoRepository.findByCodigo("PDA").orElseThrow();
        var cadastrado = ativoService.cadastrar(new AtivoService.CadastrarAtivo(
                tipoPda.getId(),
                numeroSerie,
                patrimonio,
                "Honeywell",
                "CT40",
                null
        ));
        ativoService.liberar(cadastrado.id());
        return ativoService.alocarAoSetor(
                cadastrado.id(),
                new AtivoService.AlocarSetor(setorId, "Preparacao do pool de teste")
        );
    }
}
