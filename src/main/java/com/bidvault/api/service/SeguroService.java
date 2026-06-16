package com.bidvault.api.service;

import com.bidvault.api.dto.seguro.AmpliarPolizaResponse;
import com.bidvault.api.dto.seguro.SeguroDTO;
import com.bidvault.api.entity.EstadoArticulo;
import com.bidvault.api.entity.Seguro;
import com.bidvault.api.repository.EstadoArticuloRepository;
import com.bidvault.api.repository.SeguroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SeguroService {

    private final SeguroRepository seguroRepository;
    private final EstadoArticuloRepository estadoArticuloRepository;

    public SeguroDTO obtenerSeguro(Integer productoId, Integer clienteId) {
        // Verificar que el cliente es el dueño del artículo
        EstadoArticulo estado = estadoArticuloRepository.findByProducto(productoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Artículo no encontrado"));

        if (!estado.getClientePublicador().equals(clienteId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tenés acceso al seguro de este artículo");
        }

        Seguro seguro = seguroRepository.findByProducto(productoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Este artículo aún no tiene seguro asignado"));

        SeguroDTO dto = new SeguroDTO();
        dto.setDepositoNombre(seguro.getDepositoNombre());
        dto.setDepositoDireccion(seguro.getDepositoDireccion());
        dto.setDepositoCiudad(seguro.getDepositoCiudad());
        dto.setDepositoTelefono(seguro.getDepositoTelefono());
        dto.setDepositoReferencia(seguro.getDepositoReferencia());
        dto.setAseguradora(seguro.getAseguradora());
        dto.setNumeroPoliza(seguro.getNumeroPoliza());
        dto.setMontoCobertura(seguro.getMontoCobertura());
        dto.setVigenciaDesde(seguro.getVigenciaDesde());
        dto.setVigenciaHasta(seguro.getVigenciaHasta());
        dto.setEstadoPoliza(seguro.getEstadoPoliza());
        dto.setTelefonoAseguradora(seguro.getTelefonoAseguradora());
        return dto;
    }

    public AmpliarPolizaResponse ampliarPoliza(Integer productoId, Integer clienteId) {
        // Verificar que el cliente es el dueño
        EstadoArticulo estado = estadoArticuloRepository.findByProducto(productoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Artículo no encontrado"));

        if (!estado.getClientePublicador().equals(clienteId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tenés acceso al seguro de este artículo");
        }

        // Verificar que el seguro existe
        seguroRepository.findByProducto(productoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Este artículo aún no tiene seguro asignado"));

        // En producción, aquí se notificaría a la aseguradora o se crearía una solicitud.
        // Por ahora registramos la solicitud retornando el mensaje de confirmación.
        return new AmpliarPolizaResponse(
                "Solicitud recibida. Nos comunicaremos con vos para coordinar el pago de la diferencia de prima."
        );
    }
}
