package com.bidvault.api.dto.multa;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MultaDTO {
    private Integer id;
    private BigDecimal montoMulta;
    private boolean pagada;
    private LocalDateTime fechaVencimiento;
}