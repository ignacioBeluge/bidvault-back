package com.bidvault.api.dto.puja;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PujaHistorialDTO {
    private Integer pujaId;
    private BigDecimal monto;
    private Integer numeroPostor;   // para mostrar "Postor 3"
    private boolean esMia;          // true si la puja es del usuario actual
}