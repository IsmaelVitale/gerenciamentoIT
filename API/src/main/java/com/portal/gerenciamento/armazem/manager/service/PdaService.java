package com.portal.gerenciamento.armazem.manager.service;

import com.portal.gerenciamento.armazem.manager.model.Chamado;
import com.portal.gerenciamento.armazem.manager.model.HistoricoUso;
import com.portal.gerenciamento.armazem.manager.model.Pda;
import com.portal.gerenciamento.armazem.manager.model.Usuario;
import com.portal.gerenciamento.armazem.manager.repository.ChamadoRepository;
import com.portal.gerenciamento.armazem.manager.repository.HistoricoUsoRepository;
import com.portal.gerenciamento.armazem.manager.repository.PdaRepository;
import com.portal.gerenciamento.armazem.manager.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PdaService {

    @Autowired
    private PdaRepository pdaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HistoricoUsoRepository historicoUsoRepository;

    @Autowired
    private ChamadoRepository chamadoRepository;

    public Pda cadastrarPda(Pda pda) {
        if (pdaRepository.findByPatrimonio(pda.getPatrimonio()).isPresent()) {
            throw new RuntimeException("Patrimônio já cadastrado.");
        }
        if (pda.getStatus() == null) pda.setStatus("ESTOQUE_TI");
        return pdaRepository.save(pda);
    }

    public List<Pda> listarTodas() {
        return pdaRepository.findAll();
    }

    public List<HistoricoUso> listarHistoricoCompleto() {
        return historicoUsoRepository.findAll();
    }

    public Pda atualizarPda(Long id, Pda pdaAtualizada) {
        return pdaRepository.findById(id).map(pda -> {
            pda.setModelo(pdaAtualizada.getModelo());
            pda.setStatus(pdaAtualizada.getStatus());
            pda.setSetorAlocado(pdaAtualizada.getSetorAlocado());

            // Permite resetar donos se vier nulo na atualização em massa
            pda.setDonoTurno1(pdaAtualizada.getDonoTurno1());
            pda.setDonoTurno2(pdaAtualizada.getDonoTurno2());
            return pdaRepository.save(pda);
        }).orElseThrow(() -> new RuntimeException("PDA não encontrada."));
    }

    @Transactional // Garante que tudo é apagado ou nada é apagado
    public void deletarPda(Long id) {
        Pda pda = pdaRepository.findById(id).orElseThrow(() -> new RuntimeException("PDA não encontrada."));

        // 1. Desvincula a PDA de todos os chamados existentes (para não perder o histórico do chamado)
        List<Chamado> chamados = chamadoRepository.findByPda(pda);
        for (Chamado c : chamados) {
            c.setPda(null);
            chamadoRepository.save(c);
        }

        // 2. Apaga definitivamente os logs de retirada desta PDA
        List<HistoricoUso> historico = historicoUsoRepository.findByPda(pda);
        historicoUsoRepository.deleteAll(historico);

        // 3. Deleta a PDA com segurança
        pdaRepository.delete(pda);
    }

    public Pda vincularPdaAoOperador(String matriculaLider, String matriculaOperador, String patrimonioPda) {
        Usuario lider = usuarioRepository.findByMatricula(matriculaLider).orElseThrow(() -> new RuntimeException("Líder não encontrado!"));
        Usuario operador = usuarioRepository.findByMatricula(matriculaOperador).orElseThrow(() -> new RuntimeException("Operador não encontrado!"));
        Pda pda = pdaRepository.findByPatrimonio(patrimonioPda).orElseThrow(() -> new RuntimeException("PDA não encontrada!"));

        if (pda.getSetorAlocado() != null && !lider.getSetores().contains(pda.getSetorAlocado())) {
            throw new RuntimeException("A PDA pertence a um setor que não é gerido por você.");
        }

        boolean temSetorEmComum = operador.getSetores().stream().anyMatch(setorOp -> lider.getSetores().contains(setorOp));
        if (!temSetorEmComum) {
            throw new RuntimeException("Operador pertence a um setor não gerido por você.");
        }

        if ("1".equals(operador.getTurno())) pda.setDonoTurno1(operador);
        else if ("2".equals(operador.getTurno())) pda.setDonoTurno2(operador);
        else throw new RuntimeException("Turno do operador inválido.");

        pda.setStatus("ESTOQUE_SETOR");
        return pdaRepository.save(pda);
    }

    public Pda desvincularPda(String patrimonioPda, String turno) {
        Pda pda = pdaRepository.findByPatrimonio(patrimonioPda).orElseThrow(() -> new RuntimeException("PDA não encontrada!"));

        if ("1".equals(turno)) pda.setDonoTurno1(null);
        else if ("2".equals(turno)) pda.setDonoTurno2(null);

        if ("EM_USO".equals(pda.getStatus())) {
            devolverPda(patrimonioPda, null);
        }

        if (pda.getDonoTurno1() == null && pda.getDonoTurno2() == null && !"MANUTENCAO".equals(pda.getStatus())) {
            pda.setStatus("ESTOQUE_SETOR");
        }

        return pdaRepository.save(pda);
    }

    public Pda retirarPda(String patrimonio, String matriculaOperador) {
        Pda pda = pdaRepository.findByPatrimonio(patrimonio).orElseThrow(() -> new RuntimeException("PDA não encontrada."));
        Usuario operador = usuarioRepository.findByMatricula(matriculaOperador).orElseThrow(() -> new RuntimeException("Operador não encontrado."));

        if (!"ESTOQUE_SETOR".equals(pda.getStatus())) {
            throw new RuntimeException("Esta PDA não está disponível no armário do setor (Status atual: " + pda.getStatus() + ").");
        }

        pda.setStatus("EM_USO");

        HistoricoUso hist = new HistoricoUso();
        hist.setPda(pda);
        hist.setUsuario(operador);
        hist.setTurno(operador.getTurno());
        historicoUsoRepository.save(hist);

        return pdaRepository.save(pda);
    }

    public Pda devolverPda(String patrimonio, String matriculaOperador) {
        Pda pda = pdaRepository.findByPatrimonio(patrimonio).orElseThrow(() -> new RuntimeException("PDA não encontrada."));

        pda.setStatus("ESTOQUE_SETOR");

        HistoricoUso histAberto = historicoUsoRepository.findFirstByPdaAndDataDevolucaoIsNullOrderByDataRetiradaDesc(pda);
        if (histAberto != null) {
            histAberto.setDataDevolucao(LocalDateTime.now());
            historicoUsoRepository.save(histAberto);
        }

        return pdaRepository.save(pda);
    }
}