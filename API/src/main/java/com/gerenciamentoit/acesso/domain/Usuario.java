package com.gerenciamentoit.acesso.domain;

import com.gerenciamentoit.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuarios_matricula", columnNames = "matricula")
})
public class Usuario extends BaseEntity {

    @Column(name = "matricula", nullable = false, length = 80)
    private String matricula;

    @Column(name = "nome", nullable = false, length = 160)
    private String nome;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    protected Usuario() {
    }

    public Usuario(String matricula, String nome) {
        this.matricula = matricula;
        this.nome = nome;
        this.ativo = true;
    }

    public void atualizarNome(String nome) {
        this.nome = nome;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getNome() {
        return nome;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
