package com.portal.gerenciamento.armazem.manager.service;

import com.portal.gerenciamento.armazem.manager.model.Setor;
import com.portal.gerenciamento.armazem.manager.repository.SetorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetorService {

    @Autowired
    private SetorRepository setorRepository;

    public Setor criar(Setor setor) {
        if (setorRepository.findByNome(setor.getNome()).isPresent()) {
            throw new RuntimeException("Já existe um setor com este nome.");
        }
        if (setor.getAdministrativo() == null) setor.setAdministrativo(false);
        return setorRepository.save(setor);
    }

    public List<Setor> listarTodos() {
        return setorRepository.findAll();
    }

    public Setor atualizar(Long id, Setor setorAtualizado) {
        return setorRepository.findById(id).map(setor -> {
            setor.setNome(setorAtualizado.getNome());
            setor.setCota(setorAtualizado.getCota());
            if (setorAtualizado.getAdministrativo() != null) {
                setor.setAdministrativo(setorAtualizado.getAdministrativo());
            }
            return setorRepository.save(setor);
        }).orElseThrow(() -> new RuntimeException("Setor não encontrado."));
    }

    public void deletar(Long id) {
        setorRepository.deleteById(id);
    }
}