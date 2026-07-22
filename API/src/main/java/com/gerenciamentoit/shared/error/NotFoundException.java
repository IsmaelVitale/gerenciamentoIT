package com.gerenciamentoit.shared.error;

import org.springframework.http.HttpStatus;

public class NotFoundException extends DomainException {
    public NotFoundException(String codigo, String mensagem) {
        super(HttpStatus.NOT_FOUND, codigo, mensagem);
    }
}
