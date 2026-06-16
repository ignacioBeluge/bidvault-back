package com.bidvault.api.service;

import com.bidvault.api.dto.multa.MultaDTO;
import com.bidvault.api.dto.multa.MultasResponse;
import com.bidvault.api.entity.Cliente;
import com.bidvault.api.entity.Multa;
import com.bidvault.api.repository.ClienteRepository;
import com.bidvault.api.repository.MultaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MultaService {

    private final MultaRepository multaRepository;
    private final ClienteRepository clienteRepository;

    public MultasResponse misMultas(Integer clienteId) {
        // Obtener cliente para saber si está bloqueado
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        boolean bloqueado = !"si".equals(cliente.getAdmitido());

        // Obtener multas del cliente
        List<Multa> multas = multaRepository.findByCliente(clienteId);

        List<MultaDTO> multaDTOs = multas.stream().map(m -> {
            MultaDTO dto = new MultaDTO();
            dto.setId(m.getIdentificador());
            dto.setSubastaId(m.getSubasta());
            dto.setArticulo(m.getArticulo());
            dto.setMontoOfertado(m.getMontoOfertado());
            dto.setMontoMulta(m.getMontoMulta());
            dto.setPagada("si".equals(m.getPagada()));
            dto.setFechaVencimiento(m.getFechaVencimiento());
            return dto;
        }).collect(Collectors.toList());

        return new MultasResponse(bloqueado, multaDTOs);
    }
}
