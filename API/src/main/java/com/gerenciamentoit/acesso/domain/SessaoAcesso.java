package com.gerenciamentoit.acesso.domain;

import com.gerenciamentoit.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "sessoes_acesso", uniqueConstraints = {
        @UniqueConstraint(name = "uk_sessoes_token_hash", columnNames = "token_hash")
})
public class SessaoAcesso extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem_aplicacao", nullable = false, length = 40)
    private OrigemAplicacao origemAplicacao;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "revogada_em")
    private Instant revogadaEm;

    protected SessaoAcesso() {
    }

    public SessaoAcesso(Usuario usuario, String tokenHash, OrigemAplicacao origemAplicacao, Instant expiraEm) {
        this.usuario = usuario;
        this.tokenHash = tokenHash;
        this.origemAplicacao = origemAplicacao;
        this.expiraEm = expiraEm;
    }

    public boolean estaValida(Instant agora) {
        return revogadaEm == null && expiraEm.isAfter(agora) && usuario.isAtivo();
    }

    public void revogar(Instant quando) {
        this.revogadaEm = quando;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OrigemAplicacao getOrigemAplicacao() {
        return origemAplicacao;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }

    public Instant getRevogadaEm() {
        return revogadaEm;
    }
}
