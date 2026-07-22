package com.gerenciamentoit.auditoria.domain;

import com.gerenciamentoit.acesso.domain.OrigemAplicacao;
import com.gerenciamentoit.acesso.domain.Usuario;
import com.gerenciamentoit.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "registros_auditoria")
public class RegistroAuditoria extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "acao", nullable = false, length = 100)
    private String acao;

    @Column(name = "recurso", nullable = false, length = 100)
    private String recurso;

    @Column(name = "recurso_id", length = 80)
    private String recursoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem_aplicacao", nullable = false, length = 40)
    private OrigemAplicacao origemAplicacao;

    @Column(name = "correlacao_id", length = 100)
    private String correlacaoId;

    @Column(name = "dados_anteriores", columnDefinition = "TEXT")
    private String dadosAnteriores;

    @Column(name = "dados_novos", columnDefinition = "TEXT")
    private String dadosNovos;

    @Column(name = "justificativa", length = 500)
    private String justificativa;

    protected RegistroAuditoria() {
    }

    public RegistroAuditoria(
            Usuario usuario,
            String acao,
            String recurso,
            String recursoId,
            OrigemAplicacao origemAplicacao,
            String correlacaoId,
            String dadosAnteriores,
            String dadosNovos,
            String justificativa
    ) {
        this.usuario = usuario;
        this.acao = acao;
        this.recurso = recurso;
        this.recursoId = recursoId;
        this.origemAplicacao = origemAplicacao;
        this.correlacaoId = correlacaoId;
        this.dadosAnteriores = dadosAnteriores;
        this.dadosNovos = dadosNovos;
        this.justificativa = justificativa;
    }
}
