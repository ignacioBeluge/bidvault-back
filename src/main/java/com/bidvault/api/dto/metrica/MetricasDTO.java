package com.bidvault.api.dto.metrica;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MetricasDTO {
    private int subastasParticipadas;
    private int totalPujasRealizadas;
    private int vecesGano;
    private BigDecimal totalOfertado;
    private BigDecimal totalPagado;
    private int comprasPendientes;
    private int cantidadMultas;
}