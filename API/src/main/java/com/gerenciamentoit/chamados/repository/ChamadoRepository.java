package com.gerenciamentoit.chamados.repository;

import com.gerenciamentoit.acesso.domain.OrigemAplicacao;
import com.gerenciamentoit.chamados.domain.Chamado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChamadoRepository extends JpaRepository<Chamado, UUID> {

    Optional<Chamado> findByOrigemAplicacaoAndIdentificadorExterno(
            OrigemAplicacao origemAplicacao,
            String identificadorExterno
    );

    Page<Chamado> findBySolicitante_Id(UUID solicitanteId, Pageable pageable);

    Optional<Chamado> findByIdAndSolicitante_Id(UUID id, UUID solicitanteId);

    Optional<Chamado> findByProtocoloAndSolicitante_Id(String protocolo, UUID solicitanteId);
}
