package com.portal.gerenciamento.armazem.manager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "chamados")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false, length = 20)
    private String prioridade;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, length = 30)
    private String status = "ABERTO";

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne
    @JoinColumn(name = "pda_id")
    private Pda pda;

    // NOVO: Técnico da T.I. que assumiu o chamado
    @ManyToOne
    @JoinColumn(name = "tecnico_responsavel_id")
    private Usuario tecnicoResponsavel;

    @Column(name = "data_abertura", updatable = false)
    private LocalDateTime dataAbertura = LocalDateTime.now();
}