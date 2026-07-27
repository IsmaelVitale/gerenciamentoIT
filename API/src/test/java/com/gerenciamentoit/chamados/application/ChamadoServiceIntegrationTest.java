package com.gerenciamentoit.chamados.application;

import com.gerenciamentoit.acesso.application.SessaoService;
import com.gerenciamentoit.acesso.domain.OrigemAplicacao;
import com.gerenciamentoit.chamados.domain.StatusChamado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChamadoServiceIntegrationTest {

    @Autowired
    private ChamadoService chamadoService;

    @Autowired
    private SessaoService sessaoService;

    @BeforeEach
    void autenticarGestorPeloWhatsApp() {
        SessaoService.SessaoCriada sessao = sessaoService.criar(
                "ADMIN-TESTE",
                OrigemAplicacao.WHATSAPP
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
    void abreChamadoAssociadoAoUsuarioEOrigemDaSessao() {
        var resultado = chamadoService.abrir(new ChamadoService.AbrirChamado(
                " Leitor de codigo de barras travado ",
                "+55 (11) 99999-9999",
                "mensagem-001"
        ));

        assertThat(resultado.criado()).isTrue();
        assertThat(resultado.chamado().protocolo()).startsWith("CH-");
        assertThat(resultado.chamado().solicitanteMatricula()).isEqualTo("ADMIN-TESTE");
        assertThat(resultado.chamado().descricao()).isEqualTo("Leitor de codigo de barras travado");
        assertThat(resultado.chamado().telefoneContato()).isEqualTo("5511999999999");
        assertThat(resultado.chamado().origemAplicacao()).isEqualTo(OrigemAplicacao.WHATSAPP);
        assertThat(resultado.chamado().status()).isEqualTo(StatusChamado.ABERTO);
    }

    @Test
    void reaproveitaChamadoQuandoMensagemDoWhatsAppForReprocessada() {
        var comando = new ChamadoService.AbrirChamado(
                "Impressora sem comunicacao",
                "5511988887777",
                "mensagem-idempotente-001"
        );

        var primeira = chamadoService.abrir(comando);
        var segunda = chamadoService.abrir(comando);

        assertThat(primeira.criado()).isTrue();
        assertThat(segunda.criado()).isFalse();
        assertThat(segunda.chamado().id()).isEqualTo(primeira.chamado().id());
        assertThat(segunda.chamado().protocolo()).isEqualTo(primeira.chamado().protocolo());
    }

    @Test
    void listaOsChamadosDoUsuarioAutenticado() {
        chamadoService.abrir(new ChamadoService.AbrirChamado(
                "PDA nao liga",
                null,
                "mensagem-002"
        ));

        var pagina = chamadoService.listarMeus(PageRequest.of(0, 20));

        assertThat(pagina.getContent())
                .extracting(ChamadoService.ChamadoResumo::descricao)
                .contains("PDA nao liga");
    }
}
