package com.gerenciamentoit.shared.error;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String codigo,
        String mensagem,
        String caminho,
        String correlacaoId,
        String campo,
        List<CampoInvalido> erros
) {
    public record CampoInvalido(String campo, String mensagem) {
    }
}
