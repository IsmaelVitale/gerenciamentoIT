package com.gerenciamentoit.acesso.repository;

import com.gerenciamentoit.acesso.domain.AtribuicaoAcesso;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AtribuicaoAcessoRepository extends JpaRepository<AtribuicaoAcesso, UUID> {

    @EntityGraph(attributePaths = {"setor", "turno", "usuario"})
    List<AtribuicaoAcesso> findByUsuarioIdAndAtivoTrue(UUID usuarioId);
}
