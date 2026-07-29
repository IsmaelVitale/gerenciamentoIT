package com.gerenciamentoit.conferencia.domain;

import com.gerenciamentoit.ativos.domain.Ativo;
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
@Table(name = "conferencia_itens", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_conferencia_itens_conferencia_ativo",
                columnNames = {"conferencia_id", "ativo_id"}
        )
})
public class ConferenciaItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conferencia_id", nullable = false)
    private Conferencia conferencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ativo_id", nullable = false)
    private Ativo ativo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusItemConferencia status;

    @Column(name = "confirmada_em")
    private Instant confirmadaEm;

    protected ConferenciaItem() {
    }

    public ConferenciaItem(Conferencia conferencia, Ativo ativo) {
        this.conferencia = conferencia;
        this.ativo = ativo;
        this.status = StatusItemConferencia.PENDENTE;
    }

    public void confirmar(Instant instante) {
        this.status = StatusItemConferencia.CONFIRMADA;
        this.confirmadaEm = instante;
    }

    public void marcarAusente() {
        if (status == StatusItemConferencia.PENDENTE) {
            this.status = StatusItemConferencia.AUSENTE;
        }
    }

    public Ativo getAtivo() {
        return ativo;
    }

    public StatusItemConferencia getStatus() {
        return status;
    }

    public Instant getConfirmadaEm() {
        return confirmadaEm;
    }
}
