package com.portal.gerenciamento.armazem.manager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_uso_pda")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoUso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pda_id", nullable = false)
    private Pda pda;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String turno;

    @Column(name = "data_retirada", nullable = false)
    private LocalDateTime dataRetirada = LocalDateTime.now();

    @Column(name = "data_devolucao")
    private LocalDateTime dataDevolucao;
}