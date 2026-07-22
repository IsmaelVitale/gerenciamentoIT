package com.gerenciamentoit.ativos.application;

import com.gerenciamentoit.acesso.application.SessaoService;
import com.gerenciamentoit.acesso.domain.OrigemAplicacao;
import com.gerenciamentoit.ativos.domain.DisponibilidadeAtivo;
import com.gerenciamentoit.ativos.domain.SituacaoPatrimonial;
import com.gerenciamentoit.ativos.repository.TipoAtivoRepository;
import com.gerenciamentoit.shared.error.ConflictException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AtivoServiceIntegrationTest {

    @Autowired
    private AtivoService ativoService;

    @Autowired
    private TipoAtivoRepository tipoAtivoRepository;

    @Autowired
    private SessaoService sessaoService;

    @BeforeEach
    void autenticarGestorDoBootstrap() {
        SessaoService.SessaoCriada sessao = sessaoService.criar(
                "ADMIN-TESTE",
                OrigemAplicacao.GESTAO_ATIVOS
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
    void cadastraAtivoSemPatrimonio() {
        var tipoPda = tipoAtivoRepository.findByCodigo("PDA").orElseThrow();

        var ativo = ativoService.cadastrar(new AtivoService.CadastrarAtivo(
                tipoPda.getId(),
                " sn-ct40-001 ",
                null,
                "Honeywell",
                "CT40",
                null
        ));

        assertThat(ativo.numeroSerie()).isEqualTo("SN-CT40-001");
        assertThat(ativo.patrimonio()).isNull();
        assertThat(ativo.situacaoPatrimonial()).isEqualTo(SituacaoPatrimonial.EM_PREPARACAO);
        assertThat(ativo.disponibilidade()).isEqualTo(DisponibilidadeAtivo.INDISPONIVEL);
    }

    @Test
    void impedeNumeroDeSerieDuplicado() {
        var tipoPda = tipoAtivoRepository.findByCodigo("PDA").orElseThrow();
        var comando = new AtivoService.CadastrarAtivo(
                tipoPda.getId(),
                "SN-DUPLICADO-001",
                null,
                null,
                null,
                null
        );

        ativoService.cadastrar(comando);

        assertThatThrownBy(() -> ativoService.cadastrar(comando))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("numero de serie");
    }

    @Test
    void impedePatrimonioDuplicadoQuandoPreenchido() {
        var tipoPda = tipoAtivoRepository.findByCodigo("PDA").orElseThrow();

        ativoService.cadastrar(new AtivoService.CadastrarAtivo(
                tipoPda.getId(), "SN-PAT-001", "PDA-047", null, null, null
        ));

        assertThatThrownBy(() -> ativoService.cadastrar(new AtivoService.CadastrarAtivo(
                tipoPda.getId(), "SN-PAT-002", "pda-047", null, null, null
        )))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("patrimonio");
    }
}
