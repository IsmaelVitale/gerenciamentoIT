package com.gerenciamentoit.conferencia.repository;

import com.gerenciamentoit.conferencia.domain.ConferenciaItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConferenciaItemRepository extends JpaRepository<ConferenciaItem, UUID> {

    @EntityGraph(attributePaths = {"ativo", "ativo.tipo", "ativo.setorPermanente"})
    List<ConferenciaItem> findByConferenciaIdOrderByAtivoNumeroSerieAsc(UUID conferenciaId);

    Optional<ConferenciaItem> findByConferenciaIdAndAtivoId(UUID conferenciaId, UUID ativoId);
}
