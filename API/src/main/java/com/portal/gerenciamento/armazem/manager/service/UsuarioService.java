package com.portal.gerenciamento.armazem.manager.service;

import com.portal.gerenciamento.armazem.manager.model.Pda;
import com.portal.gerenciamento.armazem.manager.model.Usuario;
import com.portal.gerenciamento.armazem.manager.repository.PdaRepository;
import com.portal.gerenciamento.armazem.manager.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PdaRepository pdaRepository;

    public Usuario criar(Usuario usuario) {
        if (usuarioRepository.findByMatricula(usuario.getMatricula()).isPresent()) {
            throw new RuntimeException("Matrícula já cadastrada.");
        }
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorMatricula(String matricula) {
        return usuarioRepository.findByMatricula(matricula)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    public Usuario atualizar(Long id, Usuario usuarioAtualizado) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNome(usuarioAtualizado.getNome());
            usuario.setRole(usuarioAtualizado.getRole());
            usuario.setTurno(usuarioAtualizado.getTurno());
            usuario.setSetores(usuarioAtualizado.getSetores());
            return usuarioRepository.save(usuario);
        }).orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    @Transactional // Desvincula as PDAs antes de apagar a pessoa, evitando Erro 500 do banco
    public void deletar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // 1. Remove o usuário de todas as PDAs onde ele é Dono no Turno 1
        List<Pda> pdasT1 = pdaRepository.findByDonoTurno1(usuario);
        for (Pda pda : pdasT1) {
            pda.setDonoTurno1(null);
            pdaRepository.save(pda);
        }

        // 2. Remove o usuário de todas as PDAs onde ele é Dono no Turno 2
        List<Pda> pdasT2 = pdaRepository.findByDonoTurno2(usuario);
        for (Pda pda : pdasT2) {
            pda.setDonoTurno2(null);
            pdaRepository.save(pda);
        }

        // 3. Agora pode excluir com segurança
        usuarioRepository.deleteById(id);
    }
}