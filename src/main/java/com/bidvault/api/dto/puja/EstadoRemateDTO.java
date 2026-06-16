package com.bidvault.api.dto.puja;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class EstadoRemateDTO {

    private boolean enRemate;          // ¿está activo el remate?
    private boolean cerrado;           // ¿ya terminó?
    private Integer segundosRestantes; // cuánto falta para cerrar

    // Resultado (solo si cerrado = true)
    private boolean ganaste;           // ¿ganó el usuario actual?
    private boolean ganaEmpresa;       // ¿ganó la empresa (sin pujas)?
    private BigDecimal montoFinal;     // precio de adjudicación
    private Integer numeroPostorGanador;
}