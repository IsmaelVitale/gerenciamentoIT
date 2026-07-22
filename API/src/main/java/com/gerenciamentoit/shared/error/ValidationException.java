package com.gerenciamentoit.shared.error;

import org.springframework.http.HttpStatus;

public class ValidationException extends DomainException {
    public ValidationException(String codigo, String mensagem) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, codigo, mensagem);
    }

    public ValidationException(String codigo, String mensagem, String campo) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, codigo, mensagem, campo);
    }
}
