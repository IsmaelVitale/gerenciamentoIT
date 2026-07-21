package com.portal.gerenciamento.armazem.manager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String matricula;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 20)
    private String role;

    // NOVO: Coleção de Setores (Muitos-para-Muitos simplificado)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_setores", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "setor_nome")
    private List<String> setores = new ArrayList<>();

    @Column(length = 10)
    private String turno;
}