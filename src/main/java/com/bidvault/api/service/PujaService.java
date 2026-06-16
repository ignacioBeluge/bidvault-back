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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final RemateItemRepository remateItemRepository;
    private static final int SEGUNDOS_PARA_CERRAR = 60;  // 1 minuto

    private static final List<String> CATEGORIAS_SIN_LIMITE = List.of("oro", "platino");

    // ── Calcula los límites del slider (los manda el servidor, no el cliente) ──
    // Recibe el subastaId y busca la categoría internamente — el cliente NO la envía.
    public BidConstraintsDTO calcularRestricciones(Integer subastaId, Integer itemId) {

        // El servidor busca la subasta y saca la categoría
        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new BusinessException("Subasta no encontrada"));

        ItemCatalogo item = itemCatalogoRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException("Ítem no encontrado"));

        BigDecimal base = item.getPrecioBase();

        // Mejor oferta actual (si no hay pujas, se parte del precio base)
        List<Pujo> pujas = pujoRepository.findByItemOrderByImporteDesc(itemId);
        BigDecimal mejorOferta = pujas.isEmpty() ? base : pujas.get(0).getImporte();

        // La categoría sale de la subasta, no del cliente
        boolean aplicanLimites = !CATEGORIAS_SIN_LIMITE.contains(subasta.getCategoria());

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

        // 3. Calcular y validar los límites de la puja
        //    (calcularRestricciones busca la subasta y su categoría internamente)
        BidConstraintsDTO limites = calcularRestricciones(subastaId, itemId);

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

        // 4. Buscar (o crear) el asistente que vincula cliente con subasta
        Asistente asistente = asistenteRepository
                .findByClienteAndSubasta(clienteId, subastaId)
                .orElseGet(() -> {
                    Asistente nuevo = new Asistente();
                    nuevo.setCliente(clienteId);
                    nuevo.setSubasta(subastaId);
                    nuevo.setNumeroPostor(generarNumeroPostor(subastaId));
                    return asistenteRepository.save(nuevo);
                });

        // 5. Crear la puja
        Pujo pujo = new Pujo();
        pujo.setAsistente(asistente.getIdentificador());
        pujo.setItem(itemId);
        pujo.setImporte(request.getMonto());
        pujo.setGanador("no");
        pujo = pujoRepository.save(pujo);

        // 6. Registrar el estado inicial de la puja como "confirmada"
        //    (en un sistema real sería "pendiente" hasta procesarla async,
        //     pero para el TP la confirmamos directo)
        EstadoPujo estado = new EstadoPujo();
        estado.setPujo(pujo.getIdentificador());
        estado.setEstado("confirmada");
        estado.setFechaEstado(LocalDateTime.now());
        estadoPujoRepository.save(estado);

        // 7. Calcular los nuevos límites para la próxima puja
        BidConstraintsDTO nuevosLimites = calcularRestricciones(subastaId, itemId);

        // 8. Armar la respuesta
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

    // Indica si el cliente puede pujar (tiene al menos un medio de pago verificado)
    public PuedePujarDTO puedePujar(Integer clienteId) {
        long mediosVerificados = medioDePagoRepository
                .countByClienteAndVerificado(clienteId, "si");

        if (mediosVerificados == 0) {
            return new PuedePujarDTO(false,
                "Necesitás un medio de pago verificado para poder pujar");
        }
        return new PuedePujarDTO(true, null);
    }

    // Devuelve el historial de pujas de un ítem (las últimas primero)
    public List<PujaHistorialDTO> obtenerHistorial(Integer itemId, Integer clienteId) {

        List<Pujo> pujas = pujoRepository.findByItemOrderByImporteDesc(itemId);
        List<PujaHistorialDTO> historial = new ArrayList<>();

        for (Pujo pujo : pujas) {
            // Buscamos el asistente para saber de quién es la puja
            Asistente asistente = asistenteRepository.findById(pujo.getAsistente())
                    .orElse(null);

            PujaHistorialDTO dto = new PujaHistorialDTO();
            dto.setPujaId(pujo.getIdentificador());
            dto.setMonto(pujo.getImporte());

            if (asistente != null) {
                dto.setNumeroPostor(asistente.getNumeroPostor());
                // Es mía si el cliente del asistente coincide con el usuario actual
                dto.setEsMia(asistente.getCliente().equals(clienteId));
            }

            historial.add(dto);
        }

        return historial;
    }

    // Calcula el estado del remate de un ítem (para el polling del front)
@Transactional
public EstadoRemateDTO obtenerEstadoRemate(Integer subastaId, Integer itemId, Integer clienteId) {

    EstadoRemateDTO dto = new EstadoRemateDTO();

    // 1. Buscar el remate del ítem
    RemateItem remate = remateItemRepository.findByItem(itemId).orElse(null);

    // Si no hay remate creado, el ítem no está en remate todavía
    if (remate == null || !"si".equals(remate.getEnRemate())) {
        dto.setEnRemate(false);
        dto.setCerrado(false);
        return dto;
    }

    // 2. Si ya está cerrado, devolver el resultado guardado
    if ("si".equals(remate.getCerrado())) {
        dto.setEnRemate(false);
        dto.setCerrado(true);
        completarResultado(dto, remate, itemId, clienteId);
        return dto;
    }

    // 3. Está en remate activo. Calcular el tiempo de referencia.
    LocalDateTime referencia;
    List<Pujo> pujas = pujoRepository.findByItemOrderByImporteDesc(itemId);

    if (!pujas.isEmpty()) {
        // Hubo pujas → la referencia es la fecha de la última puja.
        // Usamos el estado de la puja (estadosPujos tiene fechaEstado).
        Pujo ultimaPuja = pujas.get(0);
        List<EstadoPujo> estados =
                estadoPujoRepository.findByPujoOrderByFechaEstadoDesc(ultimaPuja.getIdentificador());
        referencia = estados.isEmpty()
                ? remate.getInicioRemate()
                : estados.get(0).getFechaEstado();
    } else {
        // Sin pujas → la referencia es el inicio del remate
        referencia = remate.getInicioRemate();
    }

    // 4. Calcular cuántos segundos pasaron desde la referencia
    long segundosPasados = java.time.Duration.between(referencia, LocalDateTime.now()).getSeconds();
    long segundosRestantes = SEGUNDOS_PARA_CERRAR - segundosPasados;

    if (segundosRestantes > 0) {
        // Sigue activo
        dto.setEnRemate(true);
        dto.setCerrado(false);
        dto.setSegundosRestantes((int) segundosRestantes);
    } else {
        // Se cumplió el tiempo → cerrar el remate
        cerrarRemate(remate, itemId);
        dto.setEnRemate(false);
        dto.setCerrado(true);
        completarResultado(dto, remate, itemId, clienteId);
    }

    return dto;
}

// Cierra el remate y determina el ganador
private void cerrarRemate(RemateItem remate, Integer itemId) {
    List<Pujo> pujas = pujoRepository.findByItemOrderByImporteDesc(itemId);

    if (!pujas.isEmpty()) {
        // Gana el mayor postor
        Pujo ganadora = pujas.get(0);
        ganadora.setGanador("si");
        pujoRepository.save(ganadora);

        remate.setAsistenteGanador(ganadora.getAsistente());
        remate.setGanaEmpresa("no");
    } else {
        // Nadie pujó → gana la empresa al precio base
        remate.setGanaEmpresa("si");
    }

    remate.setEnRemate("no");
    remate.setCerrado("si");
    remate.setFechaCierre(LocalDateTime.now());
    remateItemRepository.save(remate);

    // Marcar el ítem como subastado
    itemCatalogoRepository.findById(itemId).ifPresent(item -> {
        item.setSubastado("si");
        itemCatalogoRepository.save(item);
    });
}

// Completa los datos del resultado en el DTO
private void completarResultado(EstadoRemateDTO dto, RemateItem remate,
                                Integer itemId, Integer clienteId) {

        if ("si".equals(remate.getGanaEmpresa())) {
            // Ganó la empresa (sin pujas) al precio base
            dto.setGanaEmpresa(true);
            dto.setGanaste(false);
            itemCatalogoRepository.findById(itemId).ifPresent(item ->
                    dto.setMontoFinal(item.getPrecioBase()));
            return;
        }

        // Hubo ganador entre los postores
        if (remate.getAsistenteGanador() != null) {
            Asistente ganador = asistenteRepository.findById(remate.getAsistenteGanador())
                    .orElse(null);
            if (ganador != null) {
                dto.setNumeroPostorGanador(ganador.getNumeroPostor());
                // ¿El ganador es el usuario actual?
                dto.setGanaste(ganador.getCliente().equals(clienteId));
            }

            // El monto final es la puja ganadora
            List<Pujo> pujas = pujoRepository.findByItemOrderByImporteDesc(itemId);
            if (!pujas.isEmpty()) {
                dto.setMontoFinal(pujas.get(0).getImporte());
            }
        }
    }

    // ── Historial de pujas del usuario (todas las subastas) ──
    public List<MiPujaDTO> miHistorial(Integer clienteId) {
        // Todos los asistentes del cliente (una entrada por subasta a la que fue)
        List<Asistente> asistentes = asistenteRepository.findByCliente(clienteId);

        List<MiPujaDTO> resultado = new ArrayList<>();

        for (Asistente asistente : asistentes) {
            // Todas las pujas que hizo este asistente
            List<Pujo> pujas = pujoRepository.findByAsistente(asistente.getIdentificador());

            for (Pujo pujo : pujas) {
                MiPujaDTO dto = new MiPujaDTO();
                dto.setItemId(pujo.getItem());
                dto.setSubastaId(asistente.getSubasta());
                dto.setMontoOfertado(pujo.getImporte());

                // Nombre del artículo
                itemCatalogoRepository.findById(pujo.getItem()).ifPresent(item ->
                        dto.setNombreArticulo("Artículo #" + item.getProducto()));

                // ¿Ganó el remate?
                remateItemRepository.findByItem(pujo.getItem()).ifPresent(remate -> {
                    dto.setRemateCerrado("si".equals(remate.getCerrado()));
                    dto.setGane(asistente.getIdentificador()
                            .equals(remate.getAsistenteGanador()));
                });

                resultado.add(dto);
            }
        }

        return resultado;
    }

}