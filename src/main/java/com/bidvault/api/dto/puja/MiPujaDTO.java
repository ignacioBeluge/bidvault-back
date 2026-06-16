package com.bidvault.api.dto.puja;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MiPujaDTO {
    private Integer itemId;
    private Integer subastaId;
    private String nombreArticulo;
    private BigDecimal montoOfertado;
    private boolean gane;          // true si fui el ganador del remate
    private boolean remateCerrado; // true si el remate ya terminó
}
