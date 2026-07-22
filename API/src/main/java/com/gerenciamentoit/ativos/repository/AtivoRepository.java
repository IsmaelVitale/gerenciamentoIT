package com.gerenciamentoit.ativos.repository;

import com.gerenciamentoit.ativos.domain.Ativo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface AtivoRepository extends JpaRepository<Ativo, UUID>, JpaSpecificationExecutor<Ativo> {
    boolean existsByNumeroSerie(String numeroSerie);
    boolean existsByPatrimonio(String patrimonio);
    boolean existsByNumeroSerieAndIdNot(String numeroSerie, UUID id);
    boolean existsByPatrimonioAndIdNot(String patrimonio, UUID id);

    @EntityGraph(attributePaths = "tipo")
    Optional<Ativo> findWithTipoById(UUID id);
}
