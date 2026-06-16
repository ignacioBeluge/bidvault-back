package com.bidvault.api.dto.articulo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArticuloResponse {
    private Integer productoId;
    private String estado;
    private String mensaje;
}