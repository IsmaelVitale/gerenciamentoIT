package com.gerenciamentoit.shared.error;

import org.springframework.http.HttpStatus;

public class ConflictException extends DomainException {
    public ConflictException(String codigo, String mensagem) {
        super(HttpStatus.CONFLICT, codigo, mensagem);
    }

    public ConflictException(String codigo, String mensagem, String campo) {
        super(HttpStatus.CONFLICT, codigo, mensagem, campo);
    }
}
