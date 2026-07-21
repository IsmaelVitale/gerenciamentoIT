package com.portal.gerenciamento.armazem.manager.repository;

import com.portal.gerenciamento.armazem.manager.model.Chamado;
import com.portal.gerenciamento.armazem.manager.model.Pda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
    List<Chamado> findBySolicitante_Matricula(String matricula);
    List<Chamado> findByPda_Patrimonio(String patrimonio);

    // NOVO: Busca chamados por Objeto PDA
    List<Chamado> findByPda(Pda pda);
}