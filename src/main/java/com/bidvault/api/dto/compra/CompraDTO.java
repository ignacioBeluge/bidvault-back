package com.bidvault.api.dto.compra;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CompraDTO {
    private Integer id;
    private String nombreArticulo;
    private BigDecimal montoPujado;
    private BigDecimal comision;
    private BigDecimal envio;
    private BigDecimal montoTotal;
    private String estado;            // PENDIENTE, PAGADO, VENCIDO
    private LocalDateTime fechaLimite;
    private String fotoPrincipal;
}