package com.gerenciamentoit.shared.error;

import org.springframework.http.HttpStatus;

public class DomainException extends RuntimeException {

    private final HttpStatus status;
    private final String codigo;
    private final String campo;

    public DomainException(HttpStatus status, String codigo, String mensagem) {
        this(status, codigo, mensagem, null);
    }

    public DomainException(HttpStatus status, String codigo, String mensagem, String campo) {
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
        this.campo = campo;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCampo() {
        return campo;
    }
}
