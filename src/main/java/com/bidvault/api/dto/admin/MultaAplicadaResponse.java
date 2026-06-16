package com.bidvault.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MultaAplicadaResponse {
    private Integer clienteId;
    private BigDecimal montoOfertado;
    private BigDecimal montoMulta;
    private String mensaje;
}
