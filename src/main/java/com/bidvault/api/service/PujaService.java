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
    private final RegistroDeSubastaRepository registroDeSubastaRepository;
    private final NotificacionRepository notificacionRepository;
    private final ProductoRepository productoRepository;
    private final DuenioRepository duenioRepository;
    private final EstadoArticuloRepository estadoArticuloRepository;
    private final PagoVentaRepository pagoVentaRepository;

    private static final int SEGUNDOS_PARA_CERRAR = 60;  // 1 minuto
    private static final List<String> CATEGORIAS_SIN_LIMITE = List.of("oro", "platino");

    // Costo de envío fijo según la categoría de la subasta
    private static BigDecimal costoEnvioPorCategoria(String categoria) {
        return switch (categoria) {
            case "comun"    -> new BigDecimal("3000");
            case "especial" -> new BigDecimal("5000");
            case "plata"    -> new BigDecimal("8000");
            case "oro"      -> new BigDecimal("12000");
            case "platino"  -> new BigDecimal("20000");
            default          -> new BigDecimal("5000");
        };
    }

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

        // 2.5 — Validar que no esté participando en otro remate activo simultáneamente
        validarNoEstaEnOtroRemate(clienteId, itemId);

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

    // ─────────────────────────────────────────────
    // Calcula el estado del remate de un ítem (para el polling del front)
    // ─────────────────────────────────────────────
    @Transactional
    public EstadoRemateDTO obtenerEstadoRemate(Integer subastaId, Integer itemId, Integer clienteId) {

        EstadoRemateDTO dto = new EstadoRemateDTO();

        // 1. Buscar el remate del ítem
        RemateItem remate = remateItemRepository.findByItem(itemId).orElse(null);

        // Si no hay remate creado, el ítem no está en remate
        if (remate == null) {
            dto.setEnRemate(false);
            dto.setCerrado(false);
            return dto;
        }

        // 2. Si ya está cerrado, devolver el resultado guardado (CHEQUEAR ESTO PRIMERO)
        if ("si".equals(remate.getCerrado())) {
            dto.setEnRemate(false);
            dto.setCerrado(true);
            completarResultado(dto, remate, itemId, clienteId);
            return dto;
        }

        // 3. Si no está cerrado pero tampoco en remate activo
        if (!"si".equals(remate.getEnRemate())) {
            dto.setEnRemate(false);
            dto.setCerrado(false);
            return dto;
        }

        // 4. Está en remate activo. Calcular el tiempo de referencia.
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

        // 5. Calcular cuántos segundos pasaron desde la referencia
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

            // Registrar la venta completa.
            // Necesitamos el subastaId — lo sacamos del asistente.
            Asistente asistente = asistenteRepository.findById(ganadora.getAsistente()).orElseThrow();
            registrarVenta(itemId, ganadora, asistente.getSubasta());

        } else {
            // Nadie pujó → gana la empresa al precio base
            remate.setGanaEmpresa("si");
        }

        remate.setEnRemate("no");
        remate.setCerrado("si");
        remate.setFechaCierre(LocalDateTime.now());
        remateItemRepository.save(remate);

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
                String nombreArticulo = itemCatalogoRepository.findById(pujo.getItem())
                        .map(item -> "Artículo #" + item.getProducto())
                        .orElse("Artículo #" + pujo.getItem());
                dto.setNombreArticulo(nombreArticulo);

                // ¿Ganó el remate?
                remateItemRepository.findFirstByItemOrderByIdentificadorDesc(pujo.getItem()).ifPresent(remate -> {
                    dto.setRemateCerrado("si".equals(remate.getCerrado()));
                    dto.setGane(asistente.getIdentificador()
                            .equals(remate.getAsistenteGanador()));
                });

                resultado.add(dto);
            }
        }

        return resultado;
    }

    // ─────────────────────────────────────────────
    // Registra la venta completa cuando un postor gana el remate
    // ─────────────────────────────────────────────
    private void registrarVenta(Integer itemId, Pujo pujaGanadora, Integer subastaId) {

        // 1. Datos del ítem y el producto
        ItemCatalogo item = itemCatalogoRepository.findById(itemId).orElseThrow();
        Producto producto = productoRepository.findById(item.getProducto()).orElseThrow();
        Subasta subasta = subastaRepository.findById(subastaId).orElseThrow();

        // 2. Quién ganó (el cliente del asistente de la puja ganadora)
        Asistente asistente = asistenteRepository.findById(pujaGanadora.getAsistente()).orElseThrow();
        Integer compradorId = asistente.getCliente();

        // 3. Importes
        BigDecimal importePujado = pujaGanadora.getImporte();
        BigDecimal comision = item.getComision() != null ? item.getComision() : BigDecimal.ZERO;
        BigDecimal envio = costoEnvioPorCategoria(subasta.getCategoria());
        BigDecimal totalAPagar = importePujado.add(comision).add(envio);

        // 4. Registrar la venta en registroDeSubasta
        RegistroDeSubasta registro = new RegistroDeSubasta();
        registro.setSubasta(subastaId);
        registro.setDuenio(producto.getDuenio());   // el dueño vendedor
        registro.setProducto(producto.getIdentificador());
        registro.setCliente(compradorId);           // el comprador
        registro.setImporte(importePujado);
        registro.setComision(comision);
        registroDeSubastaRepository.save(registro);

        // 5. Marcar el ítem como vendido
        item.setSubastado("si");
        itemCatalogoRepository.save(item);

        // 6. Registrar al comprador como nuevo dueño del producto
        //    (si no es dueño todavía, lo damos de alta)
        if (!duenioRepository.existsById(compradorId)) {
            Duenio nuevoDuenio = new Duenio();
            nuevoDuenio.setIdentificador(compradorId);
            nuevoDuenio.setVerificacionFinanciera("no");
            nuevoDuenio.setVerificacionJudicial("no");
            nuevoDuenio.setCalificacionRiesgo(1);
            nuevoDuenio.setVerificador(1);
            duenioRepository.save(nuevoDuenio);
        }
        // El producto ahora pertenece al comprador
        producto.setDuenio(compradorId);
        productoRepository.save(producto);

        // 7. Actualizar el estado del artículo a VENDIDO (si existe)
        estadoArticuloRepository.findByProducto(producto.getIdentificador())
                .ifPresent(estado -> {
                    estado.setEstado("VENDIDO");
                    estadoArticuloRepository.save(estado);
                });

        // 8. Enviar el mensaje privado (notificación) al ganador con el detalle de pago
        String detalle = String.format(
            "¡Felicitaciones! Ganaste el artículo \"%s\".\n\n" +
            "Detalle de pago:\n" +
            "• Monto pujado: $%s\n" +
            "• Comisiones: $%s\n" +
            "• Envío: $%s\n" +
            "• TOTAL A PAGAR: $%s\n\n" +
            "Tenés 72 horas para presentar los fondos.",
            producto.getDescripcionCatalogo(),
            importePujado.toPlainString(),
            comision.toPlainString(),
            envio.toPlainString(),
            totalAPagar.toPlainString()
        );

        Notificacion notif = new Notificacion();
        notif.setPersona(compradorId);
        notif.setTipo("subasta_ganada");
        notif.setTitulo("¡Ganaste una subasta!");
        notif.setMensaje(detalle);
        notif.setLeida("no");
        notif.setDestinoNavegacion("resumen_compra");
        notif.setParametrosNavegacion("{\"productoId\":" + producto.getIdentificador() + "}");
        notif.setFechaCreacion(LocalDateTime.now());
        notificacionRepository.save(notif);

        // 9. Crear el registro de pago pendiente (72hs para pagar)
        PagoVenta pago = new PagoVenta();
        pago.setRegistroVenta(registro.getIdentificador());
        pago.setCliente(compradorId);
        pago.setMontoPujado(importePujado);
        pago.setComision(comision);
        pago.setEnvio(envio);
        pago.setMontoTotal(totalAPagar);
        pago.setEstado("PENDIENTE");
        pago.setFechaLimite(LocalDateTime.now().plusHours(72));
        pagoVentaRepository.save(pago);
    }

    // Verifica que el cliente no esté pujando en otro remate activo al mismo tiempo
    private void validarNoEstaEnOtroRemate(Integer clienteId, Integer itemIdActual) {
        // Todos los remates activos en este momento
        List<RemateItem> rematesActivos = remateItemRepository
                .findByEnRemateAndCerrado("si", "no");

        for (RemateItem remate : rematesActivos) {
            // Si es el mismo ítem en el que está pujando ahora, no cuenta
            if (remate.getItem().equals(itemIdActual)) continue;

            // ¿El cliente tiene alguna puja en este otro remate activo?
            List<Pujo> pujasDelItem = pujoRepository.findByItemOrderByImporteDesc(remate.getItem());
            for (Pujo pujo : pujasDelItem) {
                Asistente asistente = asistenteRepository.findById(pujo.getAsistente()).orElse(null);
                if (asistente != null && asistente.getCliente().equals(clienteId)) {
                    // El cliente ya está participando en otro remate activo
                    throw new BusinessException(
                        "No podés participar en dos remates al mismo tiempo. " +
                        "Esperá a que cierre el remate en el que ya estás pujando.");
                }
            }
        }
    }
}