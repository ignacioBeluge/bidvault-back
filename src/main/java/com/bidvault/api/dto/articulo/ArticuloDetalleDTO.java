package com.bidvault.api.dto.articulo;

import lombok.Data;
import java.math.BigDecimal;
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

    // Todas las fotos en base64
    private List<String> fotos;
}