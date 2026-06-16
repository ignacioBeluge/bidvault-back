package com.bidvault.api.dto.puja;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CanBidDTO {
    private boolean puedePujar;
    private String motivo;
}
