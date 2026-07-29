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

@Entity
@Table(name = "conferencia_leituras")
public class LeituraConferencia extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conferencia_id", nullable = false)
    private Conferencia conferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ativo_id")
    private Ativo ativo;

    @Column(name = "codigo_lido", nullable = false, length = 120)
    private String codigoLido;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 40)
    private ResultadoLeituraConferencia resultado;

    protected LeituraConferencia() {
    }

    public LeituraConferencia(
            Conferencia conferencia,
            Ativo ativo,
            String codigoLido,
            ResultadoLeituraConferencia resultado
    ) {
        this.conferencia = conferencia;
        this.ativo = ativo;
        this.codigoLido = codigoLido;
        this.resultado = resultado;
    }

    public Ativo getAtivo() {
        return ativo;
    }

    public String getCodigoLido() {
        return codigoLido;
    }

    public ResultadoLeituraConferencia getResultado() {
        return resultado;
    }
}
