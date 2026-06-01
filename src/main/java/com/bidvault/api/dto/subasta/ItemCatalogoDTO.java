package com.bidvault.api.dto.subasta;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemCatalogoDTO {

    private Integer id;
    private Integer producto;
    private String descripcion;       // viene de productos.descripcionCatalogo
    private BigDecimal precioBase;
    private BigDecimal comision;
    private String subastado;
    private BigDecimal mejorOfertaActual;  // la puja más alta hasta ahora
}
