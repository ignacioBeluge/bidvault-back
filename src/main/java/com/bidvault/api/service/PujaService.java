package com.bidvault.api.service;

import com.bidvault.api.dto.puja.*;
import com.bidvault.api.entity.*;
import com.bidvault.api.repository.*;
import com.bidvault.api.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PujaService {

    private final PujoRepository pujoRepository;
    private final EstadoPujoRepository estadoPujoRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final AsistenteRepository asistenteRepository;
    private final MedioDePagoRepository medioDePagoRepository;
    private final ClienteRepository clienteRepository;
    private final SubastaRepository subastaRepository;

    private static final List<String> CATEGORIAS_SIN_LIMITE = List.of("oro", "platino");

    // ── Calcula los límites del slider (los manda el servidor, no el cliente) ──
    public BidConstraintsDTO calcularRestricciones(Integer itemId, String categoriaSubasta) {

        ItemCatalogo item = itemCatalogoRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException("Ítem no encontrado"));

        BigDecimal base = item.getPrecioBase();

        // Mejor oferta actual (si no hay pujas, se parte del precio base)
        List<Pujo> pujas = pujoRepository.findByItemOrderByImporteDesc(itemId);
        BigDecimal mejorOferta = pujas.isEmpty() ? base : pujas.get(0).getImporte();

        boolean aplicanLimites = !CATEGORIAS_SIN_LIMITE.contains(categoriaSubasta);

        // puja mínima = mejor oferta + 1% del valor base
        BigDecimal incrementoMin = base.multiply(new BigDecimal("0.01"));
        BigDecimal pujaMinima = mejorOferta.add(incrementoMin);

        BidConstraintsDTO constraints = new BidConstraintsDTO();
        constraints.setMejorOfertaActual(mejorOferta);
        constraints.setValorBase(base);
        constraints.setPujaMinima(pujaMinima);
        constraints.setAplicanLimites(aplicanLimites);

        // puja máxima = mejor oferta + 20% del valor base (solo si aplican límites)
        if (aplicanLimites) {
            BigDecimal incrementoMax = base.multiply(new BigDecimal("0.20"));
            constraints.setPujaMaxima(mejorOferta.add(incrementoMax));
        }

        return constraints;
    }

    // ── Realizar una puja (el endpoint más crítico) ──
    @Transactional   // si algo falla, se revierte todo (puja + estado)
    public PujaResponse realizarPuja(Integer subastaId, Integer clienteId,
                                     Integer itemId, PujaRequest request) {

        // 1. Validar que el cliente tenga al menos un medio de pago verificado
        long mediosVerificados = medioDePagoRepository
                .countByClienteAndVerificado(clienteId, "si");
        if (mediosVerificados == 0) {
            throw new BusinessException(
                "Necesitás un medio de pago verificado para poder pujar");
        }

        // 2. Validar que el medio de pago seleccionado exista y sea del cliente
        MedioDePago medio = medioDePagoRepository.findById(request.getMedioPagoId())
                .orElseThrow(() -> new BusinessException("Medio de pago no encontrado"));
        if (!medio.getCliente().equals(clienteId)) {
            throw new BusinessException("El medio de pago no pertenece al usuario");
        }
        if (!"si".equals(medio.getVerificado())) {
            throw new BusinessException("El medio de pago no está verificado");
        }

        // 3. Traer la subasta para saber la categoría (límites)
        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new BusinessException("Subasta no encontrada"));

        // 4. Calcular y validar los límites de la puja
        BidConstraintsDTO limites = calcularRestricciones(itemId, subasta.getCategoria());

        if (request.getMonto().compareTo(limites.getPujaMinima()) < 0) {
            throw new BusinessException(
                "El monto debe ser al menos " + limites.getPujaMinima());
        }
        // El tope máximo solo aplica si la subasta NO es oro/platino
        if (limites.getAplicanLimites()
                && request.getMonto().compareTo(limites.getPujaMaxima()) > 0) {
            throw new BusinessException(
                "El monto no puede superar " + limites.getPujaMaxima());
        }

        // 5. Buscar (o crear) el asistente que vincula cliente con subasta
        Asistente asistente = asistenteRepository
                .findByClienteAndSubasta(clienteId, subastaId)
                .orElseGet(() -> {
                    Asistente nuevo = new Asistente();
                    nuevo.setCliente(clienteId);
                    nuevo.setSubasta(subastaId);
                    nuevo.setNumeroPostor(generarNumeroPostor(subastaId));
                    return asistenteRepository.save(nuevo);
                });

        // 6. Crear la puja
        Pujo pujo = new Pujo();
        pujo.setAsistente(asistente.getIdentificador());
        pujo.setItem(itemId);
        pujo.setImporte(request.getMonto());
        pujo.setGanador("no");
        pujo = pujoRepository.save(pujo);

        // 7. Registrar el estado inicial de la puja como "confirmada"
        //    (en un sistema real sería "pendiente" hasta procesarla async,
        //     pero para el TP la confirmamos directo)
        EstadoPujo estado = new EstadoPujo();
        estado.setPujo(pujo.getIdentificador());
        estado.setEstado("confirmada");
        estado.setFechaEstado(LocalDateTime.now());
        estadoPujoRepository.save(estado);

        // 8. Calcular los nuevos límites para la próxima puja
        BidConstraintsDTO nuevosLimites =
                calcularRestricciones(itemId, subasta.getCategoria());

        // 9. Armar la respuesta
        PujaResponse response = new PujaResponse();
        response.setPujaId(pujo.getIdentificador());
        response.setEstado("confirmada");
        response.setMontoOfertado(pujo.getImporte());
        response.setEsMayorPostor(true);   // recién pujó, es el mayor
        response.setRestriccionesSiguientePuja(nuevosLimites);
        response.setTimestamp(LocalDateTime.now());

        return response;
    }

    // Genera un número de postor incremental para la subasta.
    private Integer generarNumeroPostor(Integer subastaId) {
        // Simplificado: en producción contaría los asistentes de la subasta.
        return (int) (System.currentTimeMillis() % 10000);
    }
}