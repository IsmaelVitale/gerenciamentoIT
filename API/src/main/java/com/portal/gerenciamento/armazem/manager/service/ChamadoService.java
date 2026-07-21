package com.portal.gerenciamento.armazem.manager.service;

import com.portal.gerenciamento.armazem.manager.model.Chamado;
import com.portal.gerenciamento.armazem.manager.model.MensagemChamado;
import com.portal.gerenciamento.armazem.manager.model.Pda;
import com.portal.gerenciamento.armazem.manager.model.Usuario;
import com.portal.gerenciamento.armazem.manager.repository.ChamadoRepository;
import com.portal.gerenciamento.armazem.manager.repository.MensagemChamadoRepository;
import com.portal.gerenciamento.armazem.manager.repository.PdaRepository;
import com.portal.gerenciamento.armazem.manager.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChamadoService {

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PdaRepository pdaRepository;

    @Autowired
    private MensagemChamadoRepository mensagemChamadoRepository;

    // --- CREATE CHAMADO (Mantido igual) ---
    public Chamado abrirChamado(String tipo, String prioridade, String matricula, String patrimonioPda, String descricao) {
        Usuario solicitante = usuarioRepository.findByMatricula(matricula).orElseThrow(() -> new RuntimeException("Matrícula não encontrada no sistema."));
        Pda pda = null;
        if (patrimonioPda != null && !patrimonioPda.trim().isEmpty()) {
            pda = pdaRepository.findByPatrimonio(patrimonioPda).orElseThrow(() -> new RuntimeException("PDA não encontrada."));
        }
        Chamado chamado = new Chamado();
        chamado.setTipo(tipo); chamado.setPrioridade(prioridade); chamado.setDescricao(descricao);
        chamado.setSolicitante(solicitante); chamado.setPda(pda);
        return chamadoRepository.save(chamado);
    }

    // --- READ ---
    public List<Chamado> listarTodos() { return chamadoRepository.findAll(); }

    public Chamado buscarPorId(Long id) {
        return chamadoRepository.findById(id).orElseThrow(() -> new RuntimeException("Chamado não encontrado."));
    }

    // --- UPDATE COM LÓGICA DE DESTINO E ATRIBUIÇÃO ---
    public Chamado atualizarChamado(Long id, Map<String, String> payload) {
        Chamado chamado = buscarPorId(id);

        // 1. Atualizar Status
        if (payload.containsKey("status")) {
            chamado.setStatus(payload.get("status"));
        }

        // 2. Atualizar Atribuição (Assign)
        if (payload.containsKey("tecnicoResponsavelMatricula")) {
            String matTecnico = payload.get("tecnicoResponsavelMatricula");
            if (matTecnico == null || matTecnico.isEmpty()) {
                chamado.setTecnicoResponsavel(null); // Remove atribuição (Órfão)
            } else {
                Usuario tecnico = usuarioRepository.findByMatricula(matTecnico)
                        .orElseThrow(() -> new RuntimeException("Técnico não encontrado."));
                chamado.setTecnicoResponsavel(tecnico);
            }
        }

        // 3. Regra de Negócio: Destino da PDA ao Resolver
        if ("RESOLVIDO".equals(chamado.getStatus()) && chamado.getPda() != null && payload.containsKey("destinoPDA")) {
            Pda pda = chamado.getPda();
            String destino = payload.get("destinoPDA");

            switch (destino) {
                case "ESTOQUE_LIVRE":
                    pda.setStatus("ESTOQUE_TI");
                    pda.setDonoTurno1(null); pda.setDonoTurno2(null);
                    break;
                case "MANUTENCAO":
                    pda.setStatus("MANUTENCAO");
                    pda.setDonoTurno1(null); pda.setDonoTurno2(null);
                    break;
                case "ESTOQUE_DANIFICADO":
                    pda.setStatus("ESTOQUE_DANIFICADO");
                    pda.setDonoTurno1(null); pda.setDonoTurno2(null);
                    break;
                case "DEVOLVER_SETOR":
                    pda.setStatus("ESTOQUE_SETOR");
                    pda.setDonoTurno1(null); pda.setDonoTurno2(null);
                    break;
                case "DEVOLVER_COLABORADOR":
                    pda.setStatus("EM_USO");
                    // Mantém os donos originais
                    break;
            }
            pdaRepository.save(pda);
        }

        return chamadoRepository.save(chamado);
    }

    // --- DELETE ---
    public void deletarChamado(Long id) { chamadoRepository.delete(buscarPorId(id)); }

    // ==========================================
    // MÓDULO DE MENSAGENS / CHAT
    // ==========================================
    public MensagemChamado adicionarMensagem(Long chamadoId, String texto, String matriculaAutor, boolean isInterna) {
        Chamado chamado = buscarPorId(chamadoId);
        Usuario autor = usuarioRepository.findByMatricula(matriculaAutor)
                .orElseThrow(() -> new RuntimeException("Autor da mensagem não encontrado."));

        MensagemChamado msg = new MensagemChamado();
        msg.setChamado(chamado);
        msg.setAutor(autor);
        msg.setTexto(texto);
        msg.setInterna(isInterna);

        return mensagemChamadoRepository.save(msg);
    }

    public List<MensagemChamado> listarMensagens(Long chamadoId) {
        return mensagemChamadoRepository.findByChamadoIdOrderByDataHoraAsc(chamadoId);
    }
}