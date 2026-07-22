package com.gerenciamentoit.acesso.repository;

import com.gerenciamentoit.acesso.domain.SessaoAcesso;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SessaoAcessoRepository extends JpaRepository<SessaoAcesso, UUID> {

    @EntityGraph(attributePaths = "usuario")
    Optional<SessaoAcesso> findByTokenHashAndRevogadaEmIsNull(String tokenHash);

    long deleteByExpiraEmBefore(Instant limite);
}
