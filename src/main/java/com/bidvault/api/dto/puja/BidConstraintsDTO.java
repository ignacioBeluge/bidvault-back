package com.bidvault.api.dto.puja;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BidConstraintsDTO {

    // El cliente usa estos valores para el slider, NO los calcula él.
    private BigDecimal pujaMinima;        // últimaOferta + 1% del base
    private BigDecimal pujaMaxima;        // últimaOferta + 20% del base
    private BigDecimal mejorOfertaActual;
    private BigDecimal valorBase;
    private Boolean aplicanLimites;       // false para ORO y PLATINO
}