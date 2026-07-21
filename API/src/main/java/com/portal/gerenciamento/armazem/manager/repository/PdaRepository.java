package com.portal.gerenciamento.armazem.manager.repository;

import com.portal.gerenciamento.armazem.manager.model.Pda;
import com.portal.gerenciamento.armazem.manager.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PdaRepository extends JpaRepository<Pda, Long> {
    Optional<Pda> findByPatrimonio(String patrimonio);

    // Adicionado para permitir a exclusão segura de usuários
    List<Pda> findByDonoTurno1(Usuario donoTurno1);
    List<Pda> findByDonoTurno2(Usuario donoTurno2);
}