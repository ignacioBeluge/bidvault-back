package com.bidvault.api.exception;

// Excepción para errores de lógica de negocio (ej: "categoría insuficiente").
// Al extender RuntimeException no necesitamos declararla en cada método.
public class BusinessException extends RuntimeException {
    public BusinessException(String mensaje) {
        super(mensaje);
    }
}
