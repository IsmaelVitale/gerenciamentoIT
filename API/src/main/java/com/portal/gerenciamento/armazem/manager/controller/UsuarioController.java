package com.portal.gerenciamento.armazem.manager.controller;

import com.portal.gerenciamento.armazem.manager.model.Usuario;
import com.portal.gerenciamento.armazem.manager.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Usuario usuario) {
        try { return ResponseEntity.ok(usuarioService.criar(usuario)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage())); }
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{matricula}")
    public ResponseEntity<?> buscarPorMatricula(@PathVariable String matricula) {
        try { return ResponseEntity.ok(usuarioService.buscarPorMatricula(matricula)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage())); }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        try { return ResponseEntity.ok(usuarioService.atualizar(id, usuario)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage())); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            usuarioService.deletar(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}