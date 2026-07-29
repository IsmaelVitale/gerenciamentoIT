package com.gerenciamentoit.organizacao.application;

import com.gerenciamentoit.acesso.domain.AtribuicaoAcesso;
import com.gerenciamentoit.acesso.domain.Papel;
import com.gerenciamentoit.acesso.repository.AtribuicaoAcessoRepository;
import com.gerenciamentoit.organizacao.domain.Setor;
import com.gerenciamentoit.organizacao.domain.Turno;
import com.gerenciamentoit.organizacao.repository.SetorRepository;
import com.gerenciamentoit.organizacao.repository.TurnoRepository;
import com.gerenciamentoit.shared.security.ContextoAutenticacao;
import com.gerenciamentoit.shared.security.UsuarioAutenticado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ContextoOperacionalService {

    private final ContextoAutenticacao contextoAutenticacao;
    private final AtribuicaoAcessoRepository atribuicaoRepository;
    private final SetorRepository setorRepository;
    private final TurnoRepository turnoRepository;

    public ContextoOperacionalService(
            ContextoAutenticacao contextoAutenticacao,
            AtribuicaoAcessoRepository atribuicaoRepository,
            SetorRepository setorRepository,
            TurnoRepository turnoRepository
    ) {
        this.contextoAutenticacao = contextoAutenticacao;
        this.atribuicaoRepository = atribuicaoRepository;
        this.setorRepository = setorRepository;
        this.turnoRepository = turnoRepository;
    }

    @Transactional(readOnly = true)
    public List<ContextoOperacional> listarDoUsuarioAtual() {
        UsuarioAutenticado usuario = contextoAutenticacao.atual();
        if (possuiEscopoGlobal(usuario)) {
            return listarTodosAtivos();
        }

        Instant agora = Instant.now();
        return atribuicaoRepository.findByUsuarioIdAndAtivoTrue(usuario.usuarioId()).stream()
                .filter(atribuicao -> atribuicao.estaVigente(agora))
                .filter(atribuicao -> atribuicao.getSetor() != null && atribuicao.getTurno() != null)
                .filter(atribuicao -> atribuicao.getSetor().isAtivo() && atribuicao.getTurno().isAtivo())
                .map(atribuicao -> from(atribuicao.getSetor(), atribuicao.getTurno()))
                .distinct()
                .sorted(ordemContexto())
                .toList();
    }

    private boolean possuiEscopoGlobal(UsuarioAutenticado usuario) {
        return usuario.possuiPapel(Papel.SUPERVISOR)
                || usuario.possuiPapel(Papel.ANALISTA_TI)
                || usuario.possuiPapel(Papel.GESTOR_TI);
    }

    private List<ContextoOperacional> listarTodosAtivos() {
        List<Turno> turnos = turnoRepository.findByAtivoTrueOrderByCodigoAsc();
        return setorRepository.findByAtivoTrueOrderByNomeAsc().stream()
                .flatMap(setor -> turnos.stream().map(turno -> from(setor, turno)))
                .toList();
    }

    private ContextoOperacional from(Setor setor, Turno turno) {
        return new ContextoOperacional(
                setor.getId(),
                setor.getCodigo(),
                setor.getNome(),
                setor.getCotaPdas(),
                turno.getId(),
                turno.getCodigo(),
                turno.getNome(),
                turno.getHoraInicio(),
                turno.getHoraFim()
        );
    }

    private Comparator<ContextoOperacional> ordemContexto() {
        return Comparator.comparing(ContextoOperacional::setorNome)
                .thenComparing(ContextoOperacional::turnoCodigo);
    }

    public record ContextoOperacional(
            UUID setorId,
            String setorCodigo,
            String setorNome,
            int cotaPdas,
            UUID turnoId,
            String turnoCodigo,
            String turnoNome,
            LocalTime turnoHoraInicio,
            LocalTime turnoHoraFim
    ) {
    }
}
