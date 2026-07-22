package com.gerenciamentoit.ativos.domain;

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

@Entity
@Table(name = "ativos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ativos_numero_serie", columnNames = "numero_serie"),
        @UniqueConstraint(name = "uk_ativos_patrimonio", columnNames = "patrimonio")
})
public class Ativo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_ativo_id", nullable = false)
    private TipoAtivo tipo;

    @Column(name = "numero_serie", nullable = false, length = 120)
    private String numeroSerie;

    @Column(name = "patrimonio", length = 80)
    private String patrimonio;

    @Column(name = "fabricante", length = 120)
    private String fabricante;

    @Column(name = "modelo", length = 120)
    private String modelo;

    @Column(name = "observacao", length = 1000)
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_patrimonial", nullable = false, length = 40)
    private SituacaoPatrimonial situacaoPatrimonial;

    @Enumerated(EnumType.STRING)
    @Column(name = "disponibilidade", nullable = false, length = 40)
    private DisponibilidadeAtivo disponibilidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "localizacao_atual", nullable = false, length = 40)
    private LocalizacaoAtivo localizacaoAtual;

    protected Ativo() {
    }

    public Ativo(
            TipoAtivo tipo,
            String numeroSerie,
            String patrimonio,
            String fabricante,
            String modelo,
            String observacao
    ) {
        this.tipo = tipo;
        this.numeroSerie = numeroSerie;
        this.patrimonio = patrimonio;
        this.fabricante = fabricante;
        this.modelo = modelo;
        this.observacao = observacao;
        this.situacaoPatrimonial = SituacaoPatrimonial.EM_PREPARACAO;
        this.disponibilidade = DisponibilidadeAtivo.INDISPONIVEL;
        this.localizacaoAtual = LocalizacaoAtivo.TI;
    }

    public void liberar() {
        this.situacaoPatrimonial = SituacaoPatrimonial.ATIVO;
        this.disponibilidade = DisponibilidadeAtivo.DISPONIVEL;
        this.localizacaoAtual = LocalizacaoAtivo.TI;
    }

    public void corrigirIdentificacao(String numeroSerie, String patrimonio) {
        this.numeroSerie = numeroSerie;
        this.patrimonio = patrimonio;
    }

    public TipoAtivo getTipo() {
        return tipo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public String getPatrimonio() {
        return patrimonio;
    }

    public String getFabricante() {
        return fabricante;
    }

    public String getModelo() {
        return modelo;
    }

    public String getObservacao() {
        return observacao;
    }

    public SituacaoPatrimonial getSituacaoPatrimonial() {
        return situacaoPatrimonial;
    }

    public DisponibilidadeAtivo getDisponibilidade() {
        return disponibilidade;
    }

    public LocalizacaoAtivo getLocalizacaoAtual() {
        return localizacaoAtual;
    }
}
