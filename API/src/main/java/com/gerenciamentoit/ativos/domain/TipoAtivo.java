package com.gerenciamentoit.ativos.domain;

import com.gerenciamentoit.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "tipos_ativo", uniqueConstraints = {
        @UniqueConstraint(name = "uk_tipos_ativo_codigo", columnNames = "codigo")
})
public class TipoAtivo extends BaseEntity {

    @Column(name = "codigo", nullable = false, length = 60)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "controla_pool", nullable = false)
    private boolean controlaPool;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    protected TipoAtivo() {
    }

    public TipoAtivo(String codigo, String nome, boolean controlaPool) {
        this.codigo = codigo;
        this.nome = nome;
        this.controlaPool = controlaPool;
        this.ativo = true;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public boolean isControlaPool() {
        return controlaPool;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
