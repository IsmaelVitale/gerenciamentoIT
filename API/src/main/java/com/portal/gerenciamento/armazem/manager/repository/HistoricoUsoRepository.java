package com.portal.gerenciamento.armazem.manager.repository;

import com.portal.gerenciamento.armazem.manager.model.HistoricoUso;
import com.portal.gerenciamento.armazem.manager.model.Pda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoUsoRepository extends JpaRepository<HistoricoUso, Long> {
    HistoricoUso findFirstByPdaAndDataDevolucaoIsNullOrderByDataRetiradaDesc(Pda pda);

    // NOVO: Busca todos os registros de uma PDA para podermos limpar ao deletar
    List<HistoricoUso> findByPda(Pda pda);
}