package com.gerenciamentoit.organizacao.application;

import com.gerenciamentoit.acesso.application.SessaoService;
import com.gerenciamentoit.acesso.application.UsuarioService;
import com.gerenciamentoit.acesso.domain.OrigemAplicacao;
import com.gerenciamentoit.acesso.domain.Papel;
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
class ContextoOperacionalServiceIntegrationTest {

    @Autowired
    private ContextoOperacionalService contextoService;

    @Autowired
    private SetorService setorService;

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SessaoService sessaoService;

    @BeforeEach
    void autenticarGestorDoBootstrap() {
        autenticar("ADMIN-TESTE");
    }

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void gestorGlobalRecebeCombinacoesAtivasDeSetorETurno() {
        setorService.criar("REC", "Recebimento", 12);
        setorService.criar("EXP", "Expedicao", 8);
        turnoService.criar("T1", "Turno 1", LocalTime.of(6, 0), LocalTime.of(14, 0));

        var contextos = contextoService.listarDoUsuarioAtual();

        assertThat(contextos)
                .extracting(
                        ContextoOperacionalService.ContextoOperacional::setorCodigo,
                        ContextoOperacionalService.ContextoOperacional::turnoCodigo
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("EXP", "T1"),
                        org.assertj.core.groups.Tuple.tuple("REC", "T1")
                );
    }

    @Test
    void liderRecebeSomenteOContextoAtribuido() {
        var recebimento = setorService.criar("REC", "Recebimento", 12);
        setorService.criar("EXP", "Expedicao", 8);
        var turnoUm = turnoService.criar(
                "T1",
                "Turno 1",
                LocalTime.of(6, 0),
                LocalTime.of(14, 0)
        );
        turnoService.criar("T2", "Turno 2", LocalTime.of(14, 0), LocalTime.of(22, 0));

        var lider = usuarioService.criar("LIDER-001", "Lider de Recebimento");
        usuarioService.atribuir(
                lider.getId(),
                Papel.LIDER,
                recebimento.getId(),
                turnoUm.getId(),
                null,
                null
        );

        autenticar("LIDER-001");

        assertThat(contextoService.listarDoUsuarioAtual())
                .singleElement()
                .satisfies(contexto -> {
                    assertThat(contexto.setorCodigo()).isEqualTo("REC");
                    assertThat(contexto.turnoCodigo()).isEqualTo("T1");
                    assertThat(contexto.cotaPdas()).isEqualTo(12);
                });
    }

    private void autenticar(String matricula) {
        SessaoService.SessaoCriada sessao = sessaoService.criar(
                matricula,
                OrigemAplicacao.HUB_PDA
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(sessao.usuario(), sessao.token(), List.of())
        );
    }
}
