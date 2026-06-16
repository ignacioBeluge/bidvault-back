package com.bidvault.api.dto.multa;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class MultasResponse {
    private boolean bloqueado;
    private List<MultaDTO> multas;
}
