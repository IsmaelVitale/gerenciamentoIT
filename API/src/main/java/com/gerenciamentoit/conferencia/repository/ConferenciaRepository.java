package com.gerenciamentoit.conferencia.repository;

import com.gerenciamentoit.conferencia.domain.Conferencia;
import com.gerenciamentoit.conferencia.domain.StatusConferencia;
import com.gerenciamentoit.conferencia.domain.TipoConferencia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConferenciaRepository extends JpaRepository<Conferencia, UUID> {

    @EntityGraph(attributePaths = {"setor", "turno", "responsavel"})
    Optional<Conferencia> findWithContextById(UUID id);

    @EntityGraph(attributePaths = {"setor", "turno", "responsavel"})
    Optional<Conferencia> findFirstBySetorIdAndTurnoIdAndTipoAndStatusOrderByCriadoEmDesc(
            UUID setorId,
            UUID turnoId,
            TipoConferencia tipo,
            StatusConferencia status
    );
}
