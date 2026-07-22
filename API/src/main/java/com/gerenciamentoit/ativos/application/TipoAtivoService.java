package com.gerenciamentoit.ativos.application;

import com.gerenciamentoit.ativos.domain.TipoAtivo;
import com.gerenciamentoit.ativos.repository.TipoAtivoRepository;
import com.gerenciamentoit.auditoria.application.AuditoriaService;
import com.gerenciamentoit.shared.error.ConflictException;
import com.gerenciamentoit.shared.error.NotFoundException;
import com.gerenciamentoit.shared.util.Normalizer;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TipoAtivoService {

    private final TipoAtivoRepository repository;
    private final AuditoriaService auditoria;

    public TipoAtivoService(TipoAtivoRepository repository, AuditoriaService auditoria) {
        this.repository = repository;
        this.auditoria = auditoria;
    }

    @Transactional
    public TipoAtivo criar(String codigoInformado, String nomeInformado, boolean controlaPool) {
        String codigo = Normalizer.codigoObrigatorio(codigoInformado);
        String nome = Normalizer.textoObrigatorio(nomeInformado);
        if (repository.existsByCodigo(codigo)) {
            throw new ConflictException("TIPO_ATIVO_JA_CADASTRADO", "Ja existe um tipo de ativo com este codigo.", "codigo");
        }

        TipoAtivo tipo = repository.save(new TipoAtivo(codigo, nome, controlaPool));
        auditoria.registrar(
                "TIPO_ATIVO_CRIADO",
                "TIPO_ATIVO",
                tipo.getId(),
                null,
                "codigo=" + tipo.getCodigo() + ";nome=" + tipo.getNome() + ";controlaPool=" + tipo.isControlaPool(),
                null
        );
        return tipo;
    }

    @Transactional(readOnly = true)
    public TipoAtivo buscar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("TIPO_ATIVO_NAO_ENCONTRADO", "Tipo de ativo nao encontrado."));
    }

    @Transactional(readOnly = true)
    public List<TipoAtivo> listar() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "nome"));
    }
}
