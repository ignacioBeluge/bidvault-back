package com.bidvault.api.dto.puja;

import lombok.Data;

@Data
public class ItemEnRemateDTO {
    private boolean hayRemateActivo;   // ¿hay algún ítem en remate?
    private Integer itemId;            // cuál (null si no hay)
    private String descripcion;        // nombre del ítem
}