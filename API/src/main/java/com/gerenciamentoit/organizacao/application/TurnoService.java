package com.gerenciamentoit.organizacao.application;

import com.gerenciamentoit.auditoria.application.AuditoriaService;
import com.gerenciamentoit.organizacao.domain.Turno;
import com.gerenciamentoit.organizacao.repository.TurnoRepository;
import com.gerenciamentoit.shared.error.ConflictException;
import com.gerenciamentoit.shared.error.NotFoundException;
import com.gerenciamentoit.shared.error.ValidationException;
import com.gerenciamentoit.shared.util.Normalizer;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class TurnoService {

    private final TurnoRepository repository;
    private final AuditoriaService auditoriaService;

    public TurnoService(TurnoRepository repository, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public Turno criar(String codigoInformado, String nomeInformado, LocalTime inicio, LocalTime fim) {
        String codigo = Normalizer.codigoObrigatorio(codigoInformado);
        String nome = Normalizer.textoObrigatorio(nomeInformado);
        if ((inicio == null) != (fim == null)) {
            throw new ValidationException(
                    "HORARIO_TURNO_INCOMPLETO",
                    "Hora de inicio e hora de fim devem ser informadas em conjunto."
            );
        }
        if (inicio != null && inicio.equals(fim)) {
            throw new ValidationException(
                    "HORARIO_TURNO_INVALIDO",
                    "Hora de inicio e hora de fim nao podem ser iguais."
            );
        }
        if (repository.existsByCodigo(codigo)) {
            throw new ConflictException("CODIGO_TURNO_JA_CADASTRADO", "Ja existe um turno com este codigo.", "codigo");
        }

        Turno turno = repository.save(new Turno(codigo, nome, inicio, fim));
        auditoriaService.registrar(
                "TURNO_CRIADO",
                "TURNO",
                turno.getId(),
                null,
                "codigo=" + turno.getCodigo() + ";nome=" + turno.getNome(),
                null
        );
        return turno;
    }

    @Transactional(readOnly = true)
    public Turno buscar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("TURNO_NAO_ENCONTRADO", "Turno nao encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Turno> listar() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "codigo"));
    }
}
