package com.portal.gerenciamento.armazem.manager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "pdas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String patrimonio; // O código de barras (Ex: PDA-999)

    @Column(nullable = false, length = 50)
    private String modelo; // Ex: Honeywell CT40

    @Column(nullable = false, length = 30)
    private String status; // ESTOQUE_TI, ESTOQUE_SETOR, EM_USO, MANUTENCAO

    @Column(name = "setor_alocado", length = 50)
    private String setorAlocado; // Para qual setor essa máquina foi enviada

    // Relacionamentos: Quem está usando essa máquina no Turno 1 e Turno 2?
    @ManyToOne
    @JoinColumn(name = "usuario_turno1_id")
    private Usuario donoTurno1;

    @ManyToOne
    @JoinColumn(name = "usuario_turno2_id")
    private Usuario donoTurno2;

    @Column(name = "data_registro", updatable = false)
    private LocalDateTime dataRegistro = LocalDateTime.now();
}