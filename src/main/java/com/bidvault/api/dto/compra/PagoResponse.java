package com.bidvault.api.dto.compra;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PagoResponse {
    private boolean pagado;        // true si se pudo pagar
    private boolean multaAplicada; // true si se aplicó multa
    private BigDecimal montoMulta; // el monto de la multa (si aplica)
    private String mensaje;
}