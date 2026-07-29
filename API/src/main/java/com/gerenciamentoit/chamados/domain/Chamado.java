package com.gerenciamentoit.chamados.domain;

import com.gerenciamentoit.acesso.domain.OrigemAplicacao;
import com.gerenciamentoit.acesso.domain.Usuario;
import com.gerenciamentoit.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "chamados",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chamados_protocolo", columnNames = "protocolo"),
                @UniqueConstraint(
                        name = "uk_chamados_origem_identificador",
                        columnNames = {"origem_aplicacao", "identificador_externo"}
                )
        },
        indexes = {
                @Index(name = "idx_chamados_solicitante_criado", columnList = "solicitante_id, criado_em"),
                @Index(name = "idx_chamados_status_criado", columnList = "status, criado_em")
        }
)
public class Chamado extends BaseEntity {

    @Column(name = "protocolo", nullable = false, updatable = false, length = 40)
    private String protocolo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false, updatable = false)
    private Usuario solicitante;

    @Column(name = "descricao", nullable = false, length = 4000)
    private String descricao;

    @Column(name = "telefone_contato", length = 20)
    private String telefoneContato;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem_aplicacao", nullable = false, updatable = false, length = 40)
    private OrigemAplicacao origemAplicacao;

    @Column(name = "identificador_externo", updatable = false, length = 200)
    private String identificadorExterno;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private StatusChamado status;

    protected Chamado() {
    }

    public Chamado(
            String protocolo,
            Usuario solicitante,
            String descricao,
            String telefoneContato,
            OrigemAplicacao origemAplicacao,
            String identificadorExterno
    ) {
        this.protocolo = protocolo;
        this.solicitante = solicitante;
        this.descricao = descricao;
        this.telefoneContato = telefoneContato;
        this.origemAplicacao = origemAplicacao;
        this.identificadorExterno = identificadorExterno;
        this.status = StatusChamado.ABERTO;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getTelefoneContato() {
        return telefoneContato;
    }

    public OrigemAplicacao getOrigemAplicacao() {
        return origemAplicacao;
    }

    public String getIdentificadorExterno() {
        return identificadorExterno;
    }

    public StatusChamado getStatus() {
        return status;
    }
}
