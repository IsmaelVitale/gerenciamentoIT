package com.gerenciamentoit.organizacao.domain;

import com.gerenciamentoit.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "setores", uniqueConstraints = {
        @UniqueConstraint(name = "uk_setores_codigo", columnNames = "codigo"),
        @UniqueConstraint(name = "uk_setores_nome", columnNames = "nome")
})
public class Setor extends BaseEntity {

    @Column(name = "codigo", nullable = false, length = 40)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "cota_pdas", nullable = false)
    private int cotaPdas;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    protected Setor() {
    }

    public Setor(String codigo, String nome, int cotaPdas) {
        this.codigo = codigo;
        this.nome = nome;
        this.cotaPdas = cotaPdas;
        this.ativo = true;
    }

    public void atualizar(String nome, int cotaPdas, boolean ativo) {
        this.nome = nome;
        this.cotaPdas = cotaPdas;
        this.ativo = ativo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public int getCotaPdas() {
        return cotaPdas;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
