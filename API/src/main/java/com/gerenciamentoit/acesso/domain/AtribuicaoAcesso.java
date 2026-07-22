package com.gerenciamentoit.acesso.domain;

import com.gerenciamentoit.organizacao.domain.Setor;
import com.gerenciamentoit.organizacao.domain.Turno;
import com.gerenciamentoit.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "atribuicoes_acesso")
public class AtribuicaoAcesso extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel", nullable = false, length = 40)
    private Papel papel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id")
    private Setor setor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id")
    private Turno turno;

    @Column(name = "inicio_vigencia", nullable = false)
    private Instant inicioVigencia;

    @Column(name = "fim_vigencia")
    private Instant fimVigencia;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    protected AtribuicaoAcesso() {
    }

    public AtribuicaoAcesso(
            Usuario usuario,
            Papel papel,
            Setor setor,
            Turno turno,
            Instant inicioVigencia,
            Instant fimVigencia
    ) {
        this.usuario = usuario;
        this.papel = papel;
        this.setor = setor;
        this.turno = turno;
        this.inicioVigencia = inicioVigencia;
        this.fimVigencia = fimVigencia;
        this.ativo = true;
    }

    public boolean estaVigente(Instant agora) {
        return ativo
                && !inicioVigencia.isAfter(agora)
                && (fimVigencia == null || fimVigencia.isAfter(agora));
    }

    public void encerrar(Instant quando) {
        this.ativo = false;
        this.fimVigencia = quando;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Papel getPapel() {
        return papel;
    }

    public Setor getSetor() {
        return setor;
    }

    public Turno getTurno() {
        return turno;
    }

    public Instant getInicioVigencia() {
        return inicioVigencia;
    }

    public Instant getFimVigencia() {
        return fimVigencia;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
