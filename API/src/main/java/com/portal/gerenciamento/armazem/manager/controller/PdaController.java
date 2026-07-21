package com.portal.gerenciamento.armazem.manager.controller;

import com.portal.gerenciamento.armazem.manager.model.Pda;
import com.portal.gerenciamento.armazem.manager.service.PdaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pdas")
public class PdaController {

    @Autowired
    private PdaService pdaService;

    @PostMapping
    public ResponseEntity<?> cadastrarPda(@RequestBody Pda pda) {
        try { return ResponseEntity.ok(pdaService.cadastrarPda(pda)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage())); }
    }

    @GetMapping
    public ResponseEntity<List<Pda>> listarTodas() {
        return ResponseEntity.ok(pdaService.listarTodas());
    }

    @GetMapping("/historico")
    public ResponseEntity<?> listarHistorico() {
        return ResponseEntity.ok(pdaService.listarHistoricoCompleto());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarPda(@PathVariable Long id, @RequestBody Pda pda) {
        try { return ResponseEntity.ok(pdaService.atualizarPda(id, pda)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage())); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarPda(@PathVariable Long id) {
        try { pdaService.deletarPda(id); return ResponseEntity.ok().build(); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage())); }
    }

    @PostMapping("/vincular")
    public ResponseEntity<?> vincular(@RequestParam String lider, @RequestParam String operador, @RequestParam String pda) {
        try { return ResponseEntity.ok(pdaService.vincularPdaAoOperador(lider, operador, pda)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage())); }
    }

    @PostMapping("/desvincular")
    public ResponseEntity<?> desvincular(@RequestParam String pda, @RequestParam String turno) {
        try { return ResponseEntity.ok(pdaService.desvincularPda(pda, turno)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage())); }
    }

    // NOVAS ROTAS DO QUIOSQUE
    @PostMapping("/{patrimonio}/retirar")
    public ResponseEntity<?> retirarPda(@PathVariable String patrimonio, @RequestParam String matricula) {
        try { return ResponseEntity.ok(pdaService.retirarPda(patrimonio, matricula)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage())); }
    }

    @PostMapping("/{patrimonio}/devolver")
    public ResponseEntity<?> devolverPda(@PathVariable String patrimonio, @RequestParam String matricula) {
        try { return ResponseEntity.ok(pdaService.devolverPda(patrimonio, matricula)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage())); }
    }
}