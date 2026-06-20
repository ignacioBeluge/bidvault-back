package com.bidvault.api.dto.articulo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Data
public class ArticuloDetalleDTO {

    private Integer productoId;
    private String nombre;
    private String descripcionCompleta;
    private String estado;
    private BigDecimal precioPropuesto;
    private BigDecimal comisionPropuesta;
    private String motivoRechazo;

    // Datos de la subasta asignada (solo cuando está EN_SUBASTA)
    private String subastaFecha;
    private String subastaHora;
    private String subastaLugar;
    private java.math.BigDecimal valorBase;
    private java.math.BigDecimal comisionSubasta;

    private java.math.BigDecimal precioVenta;      // lo que se pagó (importe del registro)
    private java.math.BigDecimal comisionVenta;    // la comisión real cobrada


    // Todas las fotos en base64
    private List<String> fotos;
}