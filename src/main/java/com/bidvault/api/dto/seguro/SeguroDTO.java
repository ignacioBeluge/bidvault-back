package com.bidvault.api.dto.seguro;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SeguroDTO {

    // Depósito
    private String depositoNombre;
    private String depositoDireccion;
    private String depositoCiudad;
    private String depositoTelefono;
    private String depositoReferencia;

    // Póliza
    private String aseguradora;
    private String numeroPoliza;
    private BigDecimal montoCobertura;
    private LocalDate vigenciaDesde;
    private LocalDate vigenciaHasta;
    private String estadoPoliza;
    private String telefonoAseguradora;
}
