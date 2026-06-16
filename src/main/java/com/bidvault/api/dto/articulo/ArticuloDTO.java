package com.bidvault.api.dto.articulo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ArticuloDTO {

    private Integer productoId;
    private String nombre;              // descripcionCatalogo del producto
    private String descripcionCompleta;
    private String estado;              // EN_REVISION, PRECIO_ASIGNADO, etc.

    // El precio que propone la empresa (null si todavía no lo asignó)
    private BigDecimal precioPropuesto;
    private BigDecimal comisionPropuesta;

    // Motivo si fue rechazado
    private String motivoRechazo;

    // La primera foto, para mostrar en la card (en base64)
    private String fotoPrincipal;
}
