package com.gerenciamentoit.organizacao.domain;

import com.gerenciamentoit.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalTime;

@Entity
@Table(name = "turnos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_turnos_codigo", columnNames = "codigo")
})
public class Turno extends BaseEntity {

    @Column(name = "codigo", nullable = false, length = 40)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fim")
    private LocalTime horaFim;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    protected Turno() {
    }

    public Turno(String codigo, String nome, LocalTime horaInicio, LocalTime horaFim) {
        this.codigo = codigo;
        this.nome = nome;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.ativo = true;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
