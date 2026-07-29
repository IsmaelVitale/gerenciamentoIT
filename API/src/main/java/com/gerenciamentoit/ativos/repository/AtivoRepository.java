package com.gerenciamentoit.ativos.repository;

import com.gerenciamentoit.ativos.domain.Ativo;
import com.gerenciamentoit.ativos.domain.SituacaoPatrimonial;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AtivoRepository extends JpaRepository<Ativo, UUID>, JpaSpecificationExecutor<Ativo> {
    boolean existsByNumeroSerie(String numeroSerie);
    boolean existsByPatrimonio(String patrimonio);
    boolean existsByNumeroSerieAndIdNot(String numeroSerie, UUID id);
    boolean existsByPatrimonioAndIdNot(String patrimonio, UUID id);

    @EntityGraph(attributePaths = {"tipo", "setorPermanente"})
    Optional<Ativo> findWithTipoById(UUID id);

    @EntityGraph(attributePaths = {"tipo", "setorPermanente"})
    List<Ativo> findByTipoControlaPoolTrueAndSetorPermanenteIdAndSituacaoPatrimonialNotOrderByNumeroSerieAsc(
            UUID setorId,
            SituacaoPatrimonial situacao
    );

    @EntityGraph(attributePaths = {"tipo", "setorPermanente"})
    List<Ativo> findByNumeroSerieOrPatrimonio(String numeroSerie, String patrimonio);
}
