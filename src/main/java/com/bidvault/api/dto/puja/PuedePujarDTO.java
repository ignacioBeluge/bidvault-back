package com.bidvault.api.dto.puja;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PuedePujarDTO {
    private boolean puedePujar;
    private String motivo;   // null si puede, o el motivo si no puede
}
