package com.portal.gerenciamento.armazem.manager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensagens_chamado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensagemChamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(name = "data_hora", updatable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    // NOVO: Define se a mensagem é só para a T.I. ver
    @Column(name = "is_interna", nullable = false)
    private boolean isInterna = false;

    @ManyToOne
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;
}