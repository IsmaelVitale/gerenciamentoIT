package com.gerenciamentoit.organizacao.application;

import com.gerenciamentoit.auditoria.application.AuditoriaService;
import com.gerenciamentoit.organizacao.domain.Setor;
import com.gerenciamentoit.organizacao.repository.SetorRepository;
import com.gerenciamentoit.shared.error.ConflictException;
import com.gerenciamentoit.shared.error.NotFoundException;
import com.gerenciamentoit.shared.error.ValidationException;
import com.gerenciamentoit.shared.util.Normalizer;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SetorService {

    private final SetorRepository repository;
    private final AuditoriaService auditoriaService;

    public SetorService(SetorRepository repository, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public Setor criar(String codigoInformado, String nomeInformado, int cotaPdas) {
        String codigo = Normalizer.codigoObrigatorio(codigoInformado);
        String nome = Normalizer.textoObrigatorio(nomeInformado);
        if (cotaPdas < 0) {
            throw new ValidationException("COTA_PDA_INVALIDA", "A cota de PDAs nao pode ser negativa.", "cotaPdas");
        }

        if (repository.existsByCodigo(codigo)) {
            throw new ConflictException("CODIGO_SETOR_JA_CADASTRADO", "Ja existe um setor com este codigo.", "codigo");
        }
        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new ConflictException("NOME_SETOR_JA_CADASTRADO", "Ja existe um setor com este nome.", "nome");
        }

        Setor setor = repository.save(new Setor(codigo, nome, cotaPdas));
        auditoriaService.registrar(
                "SETOR_CRIADO",
                "SETOR",
                setor.getId(),
                null,
                "codigo=" + setor.getCodigo() + ";nome=" + setor.getNome() + ";cotaPdas=" + setor.getCotaPdas(),
                null
        );
        return setor;
    }

    @Transactional(readOnly = true)
    public Setor buscar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("SETOR_NAO_ENCONTRADO", "Setor nao encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Setor> listar() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "nome"));
    }
}
