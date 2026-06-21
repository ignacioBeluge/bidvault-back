package com.bidvault.api.service;

import com.bidvault.api.dto.compra.CompraDTO;
import com.bidvault.api.dto.compra.PagoResponse;
import com.bidvault.api.entity.*;
import com.bidvault.api.exception.BusinessException;
import com.bidvault.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final PagoVentaRepository pagoVentaRepository;
    private final RegistroDeSubastaRepository registroDeSubastaRepository;
    private final ProductoRepository productoRepository;
    private final FotoRepository fotoRepository;
    private final MedioDePagoRepository medioDePagoRepository;
    private final ChequeCertificadoRepository chequeCertificadoRepository;
    private final MultaRepository multaRepository;
    private final ClienteRepository clienteRepository;
    private final PujoRepository pujoRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;

    // Lista las compras (productos ganados) del usuario con su estado de pago
    public List<CompraDTO> misCompras(Integer clienteId) {
        List<PagoVenta> pagos = pagoVentaRepository.findByCliente(clienteId);
        List<CompraDTO> resultado = new ArrayList<>();

        for (PagoVenta pago : pagos) {
            CompraDTO dto = new CompraDTO();
            dto.setId(pago.getIdentificador());
            dto.setMontoPujado(pago.getMontoPujado());
            dto.setComision(pago.getComision());
            dto.setEnvio(pago.getEnvio());
            dto.setMontoTotal(pago.getMontoTotal());
            dto.setEstado(pago.getEstado());
            dto.setFechaLimite(pago.getFechaLimite());

            // Buscar el producto a través del registro de venta
            registroDeSubastaRepository.findById(pago.getRegistroVenta()).ifPresent(registro -> {
                productoRepository.findById(registro.getProducto()).ifPresent(producto -> {
                    dto.setNombreArticulo(producto.getDescripcionCatalogo());

                    // Primera foto
                    List<Foto> fotos = fotoRepository.findByProducto(producto.getIdentificador());
                    if (!fotos.isEmpty()) {
                        String base64 = Base64.getEncoder().encodeToString(fotos.get(0).getFoto());
                        dto.setFotoPrincipal("data:image/jpeg;base64," + base64);
                    }
                });
            });

            resultado.add(dto);
        }
        return resultado;
    }

    // El usuario intenta pagar una compra
    @Transactional
    public PagoResponse pagar(Integer pagoId, Integer clienteId) {

        PagoVenta pago = pagoVentaRepository.findById(pagoId)
                .orElseThrow(() -> new BusinessException("Pago no encontrado"));

        // Validar que el pago sea del usuario
        if (!pago.getCliente().equals(clienteId)) {
            throw new BusinessException("Este pago no te pertenece");
        }

        // Validar que esté pendiente
        if (!"PENDIENTE".equals(pago.getEstado())) {
            throw new BusinessException("Este pago ya fue procesado");
        }

        // Calcular la garantía disponible (suma de cheques certificados)
        BigDecimal garantia = calcularGarantia(clienteId);

        // ¿La garantía cubre el monto total?
        if (garantia.compareTo(pago.getMontoTotal()) >= 0) {
            // Alcanza → pago exitoso
            pago.setEstado("PAGADO");
            pago.setFechaPago(LocalDateTime.now());
            pagoVentaRepository.save(pago);

            return new PagoResponse(true, false, null,
                "Pago realizado con éxito. ¡Gracias!");
        } else {
            // No alcanza → aplicar multa del 10% del monto pujado y bloquear
            BigDecimal montoMulta = pago.getMontoPujado()
                    .multiply(new BigDecimal("0.10"))
                    .setScale(2, RoundingMode.HALF_UP);

            // Buscar la puja ganadora para vincular la multa.
            // Llegamos al producto vía el registro de venta, y de ahí al ítem y su puja.
            Integer pujoId = buscarPujoGanadora(pago.getRegistroVenta());

            Multa multa = new Multa();
            multa.setCliente(clienteId);
            multa.setPujo(pujoId);
            multa.setImporteMulta(montoMulta);
            multa.setPagada("no");
            multa.setFechaAplicacion(LocalDateTime.now());
            multa.setFechaLimite(LocalDateTime.now().plusHours(72));
            multaRepository.save(multa);

            pago.setEstado("VENCIDO");
            pagoVentaRepository.save(pago);

            clienteRepository.findById(clienteId).ifPresent(cliente -> {
                cliente.setAdmitido("no");
                clienteRepository.save(cliente);
            });

            return new PagoResponse(false, true, montoMulta,
                "No tenés fondos suficientes. Se aplicó una multa del 10% ($" +
                montoMulta + ") y tu cuenta fue bloqueada.");
        }
    }

    // Calcula la garantía disponible: suma de los cheques certificados del cliente
    private BigDecimal calcularGarantia(Integer clienteId) {
        List<MedioDePago> cheques = medioDePagoRepository
                .findByClienteAndTipo(clienteId, "cheque_certificado");

        BigDecimal total = BigDecimal.ZERO;
        for (MedioDePago medio : cheques) {
            if (!"si".equals(medio.getVerificado())) continue;  // solo verificados
            ChequeCertificado cheque = chequeCertificadoRepository
                    .findById(medio.getIdentificador()).orElse(null);
            if (cheque != null && cheque.getMonto() != null) {
                total = total.add(cheque.getMonto());
            }
        }
        return total;
    }

    // Busca la puja ganadora de una venta (para vincular la multa)
    private Integer buscarPujoGanadora(Integer registroVentaId) {
        RegistroDeSubasta registro = registroDeSubastaRepository.findById(registroVentaId)
                .orElse(null);
        if (registro == null) return null;

        // Buscar el ítem de ese producto
        List<ItemCatalogo> items = itemCatalogoRepository.findByProducto(registro.getProducto());
        if (items.isEmpty()) return null;

        // La puja ganadora es la mayor del ítem
        List<Pujo> pujos = pujoRepository.findByItemOrderByImporteDesc(items.get(0).getIdentificador());
        return pujos.isEmpty() ? null : pujos.get(0).getIdentificador();
    }
}