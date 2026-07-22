package com.gerenciamentoit.organizacao.repository;

import com.gerenciamentoit.organizacao.domain.Turno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TurnoRepository extends JpaRepository<Turno, UUID> {
    Optional<Turno> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
