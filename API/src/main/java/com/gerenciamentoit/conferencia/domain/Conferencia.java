package com.gerenciamentoit.conferencia.domain;

import com.gerenciamentoit.acesso.domain.Usuario;
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
@Table(name = "conferencias")
public class Conferencia extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turno_id", nullable = false)
    private Turno turno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoConferencia tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusConferencia status;

    @Column(name = "concluida_em")
    private Instant concluidaEm;

    protected Conferencia() {
    }

    public Conferencia(Setor setor, Turno turno, Usuario responsavel, TipoConferencia tipo) {
        this.setor = setor;
        this.turno = turno;
        this.responsavel = responsavel;
        this.tipo = tipo;
        this.status = StatusConferencia.EM_ANDAMENTO;
    }

    public void concluir(Instant instante) {
        this.status = StatusConferencia.CONCLUIDA;
        this.concluidaEm = instante;
    }

    public Setor getSetor() {
        return setor;
    }

    public Turno getTurno() {
        return turno;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public TipoConferencia getTipo() {
        return tipo;
    }

    public StatusConferencia getStatus() {
        return status;
    }

    public Instant getConcluidaEm() {
        return concluidaEm;
    }
}
