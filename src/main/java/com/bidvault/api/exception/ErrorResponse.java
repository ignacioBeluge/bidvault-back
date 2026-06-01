package com.bidvault.api.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private int codigo;                  // código HTTP (400, 401, etc.)
    private String mensaje;              // mensaje principal del error
    private Map<String, String> detalles; // errores por campo (validaciones)
    private LocalDateTime timestamp;
}
