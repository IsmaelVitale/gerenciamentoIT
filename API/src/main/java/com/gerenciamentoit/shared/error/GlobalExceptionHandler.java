package com.gerenciamentoit.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomain(DomainException ex, HttpServletRequest request) {
        return response(ex.getStatus(), ex.getCodigo(), ex.getMessage(), request, ex.getCampo(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.CampoInvalido> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiError.CampoInvalido(error.getField(), error.getDefaultMessage()))
                .toList();
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "DADOS_INVALIDOS",
                "A requisicao contem dados invalidos.", request, null, erros);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        List<ApiError.CampoInvalido> erros = ex.getConstraintViolations().stream()
                .map(error -> new ApiError.CampoInvalido(error.getPropertyPath().toString(), error.getMessage()))
                .toList();
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "DADOS_INVALIDOS",
                "A requisicao contem dados invalidos.", request, null, erros);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "DADOS_INVALIDOS",
                ex.getMessage(), request, null, List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "CORPO_INVALIDO",
                "O corpo da requisicao nao pode ser interpretado.", request, null, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, "ACESSO_NEGADO",
                "Voce nao possui permissao para executar esta acao.", request, null, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String campo = ex.getName();
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "PARAMETRO_INVALIDO",
                "O parametro '" + campo + "' possui um valor invalido.", request, campo, List.of());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex,
            HttpServletRequest request
    ) {
        return response(HttpStatus.CONFLICT, "RECURSO_ATUALIZADO_POR_OUTRO_USUARIO",
                "O recurso foi alterado por outra operacao. Recarregue os dados e tente novamente.",
                request, null, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "CONFLITO_DE_DADOS",
                "A operacao viola uma restricao de integridade.", request, null, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado. correlationId={}", MDC.get("correlationId"), ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                "Ocorreu um erro interno inesperado.", request, null, List.of());
    }

    private ResponseEntity<ApiError> response(
            HttpStatus status,
            String codigo,
            String mensagem,
            HttpServletRequest request,
            String campo,
            List<ApiError.CampoInvalido> erros
    ) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                codigo,
                mensagem,
                request.getRequestURI(),
                MDC.get("correlationId"),
                campo,
                erros
        );
        return ResponseEntity.status(status).body(body);
    }
}
