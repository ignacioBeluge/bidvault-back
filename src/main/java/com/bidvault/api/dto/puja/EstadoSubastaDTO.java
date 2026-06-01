package com.bidvault.api.dto.puja;

import com.bidvault.api.dto.subasta.ItemCatalogoDTO;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class EstadoSubastaDTO {

    private Integer subastaId;
    private ItemCatalogoDTO itemActual;
    private BigDecimal mejorOferta;
    private BidConstraintsDTO restriccionesPuja;
    private Integer totalPostores;
}
