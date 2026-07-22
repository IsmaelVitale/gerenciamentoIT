package com.gerenciamentoit.ativos.domain;

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
@Table(name = "movimentacoes_ativo")
public class MovimentacaoAtivo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ativo_id", nullable = false)
    private Ativo ativo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 50)
    private TipoMovimentacaoAtivo tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem_aplicacao", nullable = false, length = 40)
    private OrigemAplicacao origemAplicacao;

    @Column(name = "descricao", nullable = false, length = 1000)
    private String descricao;

    @Column(name = "correlacao_id", length = 100)
    private String correlacaoId;

    protected MovimentacaoAtivo() {
    }

    public MovimentacaoAtivo(
            Ativo ativo,
            Usuario usuario,
            TipoMovimentacaoAtivo tipo,
            OrigemAplicacao origemAplicacao,
            String descricao,
            String correlacaoId
    ) {
        this.ativo = ativo;
        this.usuario = usuario;
        this.tipo = tipo;
        this.origemAplicacao = origemAplicacao;
        this.descricao = descricao;
        this.correlacaoId = correlacaoId;
    }
}
