package com.gerenciamentoit.organizacao.repository;

import com.gerenciamentoit.organizacao.domain.Setor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SetorRepository extends JpaRepository<Setor, UUID> {
    Optional<Setor> findByCodigo(String codigo);
    List<Setor> findByAtivoTrueOrderByNomeAsc();
    boolean existsByCodigo(String codigo);
    boolean existsByNomeIgnoreCase(String nome);
}
