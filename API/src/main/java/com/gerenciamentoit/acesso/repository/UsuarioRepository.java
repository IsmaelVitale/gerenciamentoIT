package com.gerenciamentoit.acesso.repository;

import com.gerenciamentoit.acesso.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByMatricula(String matricula);
    boolean existsByMatricula(String matricula);
}
