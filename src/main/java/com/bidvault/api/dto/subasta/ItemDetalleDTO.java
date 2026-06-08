package com.bidvault.api.dto.subasta;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ItemDetalleDTO {

    private Integer id;
    private Integer producto;
    private String descripcionCatalogo;
    private String descripcionCompleta;
    private BigDecimal precioBase;
    private BigDecimal comision;
    private String subastado;
    private BigDecimal mejorOfertaActual;

    // Las fotos en base64 (data URI listo para usar en <Image>)
    private List<String> fotos;
}