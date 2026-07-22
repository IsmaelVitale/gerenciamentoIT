package com.gerenciamentoit.ativos.repository;

import com.gerenciamentoit.ativos.domain.MovimentacaoAtivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MovimentacaoAtivoRepository extends JpaRepository<MovimentacaoAtivo, UUID> {
}
