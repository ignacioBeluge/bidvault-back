package com.bidvault.api.service;

import com.bidvault.api.dto.admin.MultaAplicadaResponse;
import com.bidvault.api.entity.*;
import com.bidvault.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMultaService {

    private final RemateItemRepository remateItemRepository;
    private final AsistenteRepository asistenteRepository;
    private final PujoRepository pujoRepository;
    private final ClienteRepository clienteRepository;
    private final MultaRepository multaRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;

    @Transactional
    public MultaAplicadaResponse aplicarMulta(Integer remateItemId) {

        // 1. Obtener el remate
        RemateItem remate = remateItemRepository.findById(remateItemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Remate no encontrado"));

        // 2. Verificar que esté cerrado y tenga ganador
        if (!"si".equals(remate.getCerrado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El remate aún no está cerrado");
        }
        if (remate.getAsistenteGanador() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Este remate no tiene ganador");
        }

        // 3. Obtener el asistente ganador → cliente
        Asistente asistente = asistenteRepository.findById(remate.getAsistenteGanador())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Asistente ganador no encontrado"));

        Integer clienteId = asistente.getCliente();

        // 4. Verificar que no tenga ya una multa para este remate
        List<Multa> multasExistentes = multaRepository.findByCliente(clienteId);
        boolean yaMultado = multasExistentes.stream()
                .anyMatch(m -> remate.getItem().equals(m.getSubasta()));
        if (yaMultado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe una multa para este remate y cliente");
        }

        // 5. Obtener la puja ganadora (la mayor del ítem)
        List<Pujo> pujos = pujoRepository.findByItemOrderByImporteDesc(remate.getItem());
        if (pujos.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No hay pujas registradas para este ítem");
        }
        BigDecimal montoOfertado = pujos.get(0).getImporte();

        // 6. Calcular multa: 10% del monto ofertado
        BigDecimal montoMulta = montoOfertado
                .multiply(BigDecimal.valueOf(0.10))
                .setScale(2, RoundingMode.HALF_UP);

        // 7. Obtener nombre del artículo para desnormalizar
        String nombreArticulo = itemCatalogoRepository.findById(remate.getItem())
                .map(item -> "Artículo #" + item.getProducto())
                .orElse("Artículo desconocido");

        // 8. Crear la multa
        Multa multa = new Multa();
        multa.setCliente(clienteId);
        multa.setSubasta(remate.getItem());   // usamos el itemId como referencia
        multa.setArticulo(nombreArticulo);
        multa.setMontoOfertado(montoOfertado);
        multa.setMontoMulta(montoMulta);
        multa.setPagada("no");
        multa.setFechaCreacion(LocalDateTime.now());
        multa.setFechaVencimiento(LocalDateTime.now().plusHours(72));
        multaRepository.save(multa);

        // 9. Bloquear al cliente
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente no encontrado"));
        cliente.setAdmitido("no");
        clienteRepository.save(cliente);

        return new MultaAplicadaResponse(
                clienteId,
                montoOfertado,
                montoMulta,
                "Multa aplicada. Cliente bloqueado hasta regularizar el pago."
        );
    }
}
