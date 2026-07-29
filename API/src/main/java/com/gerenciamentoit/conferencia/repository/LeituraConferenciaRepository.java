package com.gerenciamentoit.conferencia.repository;

import com.gerenciamentoit.conferencia.domain.LeituraConferencia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeituraConferenciaRepository extends JpaRepository<LeituraConferencia, UUID> {

    boolean existsByConferenciaIdAndAtivoId(UUID conferenciaId, UUID ativoId);

    @EntityGraph(attributePaths = {"ativo", "ativo.tipo", "ativo.setorPermanente"})
    List<LeituraConferencia> findByConferenciaIdOrderByCriadoEmDesc(UUID conferenciaId);
}
