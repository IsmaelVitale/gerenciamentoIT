package com.gerenciamentoit.shared.util;

import java.text.Normalizer.Form;
import java.util.Locale;

public final class Normalizer {

    private Normalizer() {
    }

    public static String codigoObrigatorio(String valor) {
        String normalizado = codigoOpcional(valor);
        if (normalizado == null) {
            throw new IllegalArgumentException("Valor obrigatorio nao informado.");
        }
        return normalizado;
    }

    public static String codigoOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = java.text.Normalizer.normalize(valor, Form.NFKC)
                .trim()
                .toUpperCase(Locale.ROOT);
        return normalizado.isBlank() ? null : normalizado;
    }

    public static String textoObrigatorio(String valor) {
        String normalizado = textoOpcional(valor);
        if (normalizado == null) {
            throw new IllegalArgumentException("Valor obrigatorio nao informado.");
        }
        return normalizado;
    }

    public static String textoOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = java.text.Normalizer.normalize(valor, Form.NFKC).trim();
        return normalizado.isBlank() ? null : normalizado;
    }
}
