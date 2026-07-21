package com.portal.gerenciamento.armazem.manager.repository;

import com.portal.gerenciamento.armazem.manager.model.MensagemChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensagemChamadoRepository extends JpaRepository<MensagemChamado, Long> {

    // Traz o histórico do chat ordenado do mais antigo para o mais recente
    List<MensagemChamado> findByChamadoIdOrderByDataHoraAsc(Long chamadoId);
}