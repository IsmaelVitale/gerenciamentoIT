package com.gerenciamentoit.auditoria.repository;

import com.gerenciamentoit.auditoria.domain.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, UUID> {
}
