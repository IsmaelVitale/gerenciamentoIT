package com.portal.gerenciamento.armazem.manager.controller;

import com.portal.gerenciamento.armazem.manager.model.Chamado;
import com.portal.gerenciamento.armazem.manager.model.MensagemChamado;
import com.portal.gerenciamento.armazem.manager.service.ChamadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    @Autowired
    private ChamadoService chamadoService;

    // --- CRUD BÁSICO ---
    @PostMapping
    public ResponseEntity<?> registrarChamado(@RequestBody Map<String, String> payload) {
        try {
            return ResponseEntity.ok(chamadoService.abrirChamado(
                    payload.get("tipo"), payload.get("prioridade"),
                    payload.get("matricula"), payload.get("pda"), payload.get("descricao")
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Chamado>> listarTodos() {
        return ResponseEntity.ok(chamadoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(chamadoService.buscarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarChamado(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            return ResponseEntity.ok(chamadoService.atualizarChamado(id, payload));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarChamado(@PathVariable Long id) {
        try {
            chamadoService.deletarChamado(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // --- MENSAGENS / CHAT ---
    @GetMapping("/{id}/mensagens")
    public ResponseEntity<List<MensagemChamado>> listarMensagens(@PathVariable Long id) {
        return ResponseEntity.ok(chamadoService.listarMensagens(id));
    }

    @PostMapping("/{id}/mensagens")
    public ResponseEntity<?> adicionarMensagem(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            String texto = (String) payload.get("texto");
            String matriculaAutor = (String) payload.get("autorMatricula");
            boolean isInterna = (Boolean) payload.getOrDefault("isInterna", false);

            MensagemChamado msgSalva = chamadoService.adicionarMensagem(id, texto, matriculaAutor, isInterna);
            return ResponseEntity.ok(msgSalva);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}