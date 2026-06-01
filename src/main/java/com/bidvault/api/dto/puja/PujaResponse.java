package com.bidvault.api.dto.puja;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PujaResponse {

    private Integer pujaId;
    private String estado;             // pendiente, confirmada, superada, rechazada
    private BigDecimal montoOfertado;
    private Boolean esMayorPostor;

    // Los nuevos límites para la siguiente puja (actualiza el slider).
    private BidConstraintsDTO restriccionesSiguientePuja;

    private LocalDateTime timestamp;
}
