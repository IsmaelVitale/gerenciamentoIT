package com.gerenciamentoit.ativos.repository;

import com.gerenciamentoit.ativos.domain.TipoAtivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TipoAtivoRepository extends JpaRepository<TipoAtivo, UUID> {
    Optional<TipoAtivo> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
