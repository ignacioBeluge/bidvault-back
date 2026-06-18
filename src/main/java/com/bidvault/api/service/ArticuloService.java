package com.bidvault.api.service;

import com.bidvault.api.dto.articulo.*;
import com.bidvault.api.entity.*;
import com.bidvault.api.repository.*;
import com.bidvault.api.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticuloService {

    private final ProductoRepository productoRepository;
    private final FotoRepository fotoRepository;
    private final DuenioRepository duenioRepository;
    private final EstadoArticuloRepository estadoArticuloRepository;
    private final DeclaracionJuradaRepository declaracionJuradaRepository;

    private static final int MIN_FOTOS = 6;

    // ─────────────────────────────────────────────
    // PUBLICAR un artículo (crea producto + fotos + estado + declaración)
    // ─────────────────────────────────────────────
    @Transactional
    public ArticuloResponse publicar(Integer clienteId, PublicarArticuloRequest request) {

        // 1. Validar las declaraciones juradas
        if (!Boolean.TRUE.equals(request.getDeclaraPropiedad())) {
            throw new BusinessException("Debés declarar que sos el propietario del artículo");
        }
        if (!Boolean.TRUE.equals(request.getDeclaraOrigenLicito())) {
            throw new BusinessException("Debés declarar el origen lícito del artículo");
        }

        // 2. Validar la cantidad mínima de fotos
        if (request.getFotos() == null || request.getFotos().size() < MIN_FOTOS) {
            throw new BusinessException(
                "Debés subir al menos " + MIN_FOTOS + " fotos del artículo");
        }

        // 3. Asegurar que el cliente esté registrado como dueño.
        //    Si no existe en la tabla duenios, lo creamos (reutiliza su id de persona).
        if (!duenioRepository.existsById(clienteId)) {
            Duenio duenio = new Duenio();
            duenio.setIdentificador(clienteId);   // mismo id que la persona/cliente
            duenio.setVerificacionFinanciera("no");
            duenio.setVerificacionJudicial("no");
            duenio.setCalificacionRiesgo(1);
            duenio.setVerificador(1);             // empleado verificador por defecto
            duenioRepository.save(duenio);
        }

        // 4. Crear el producto
        Producto producto = new Producto();
        producto.setFecha(LocalDate.now());
        producto.setDisponible("no");                       // todavía no disponible
        producto.setDescripcionCatalogo(request.getNombre());
        producto.setDescripcionCompleta(request.getDescripcion());
        producto.setRevisor(1);                             // empleado revisor por defecto
        producto.setDuenio(clienteId);                      // el cliente es el dueño
        producto.setSeguro(null);                           // sin seguro hasta que se acepte
        producto = productoRepository.save(producto);

        // 5. Guardar las fotos (base64 → binario)
        for (String fotoBase64 : request.getFotos()) {
            try {
                Foto foto = new Foto();
                foto.setProducto(producto.getIdentificador());
                foto.setFoto(Base64.getDecoder().decode(fotoBase64));
                fotoRepository.save(foto);
            } catch (IllegalArgumentException e) {
                // si una foto viene mal formada, la salteamos
            }
        }

        // 6. Guardar la declaración jurada
        DeclaracionJurada declaracion = new DeclaracionJurada();
        declaracion.setProducto(producto.getIdentificador());
        declaracion.setCliente(clienteId);
        declaracion.setDeclaraPropiedad("si");
        declaracion.setDeclaraOrigenLicito("si");
        declaracion.setFechaDeclaracion(LocalDateTime.now());
        declaracionJuradaRepository.save(declaracion);

        // 7. Crear el estado inicial: EN_REVISION
        EstadoArticulo estado = new EstadoArticulo();
        estado.setProducto(producto.getIdentificador());
        estado.setEstado("EN_REVISION");
        estado.setClientePublicador(clienteId);
        estado.setFecha(LocalDateTime.now());
        estadoArticuloRepository.save(estado);

        return new ArticuloResponse(
                producto.getIdentificador(),
                "EN_REVISION",
                "Tu artículo fue enviado y está en revisión por la empresa."
        );
    }

    // ─────────────────────────────────────────────
    // LISTAR mis artículos
    // ─────────────────────────────────────────────
    public List<ArticuloDTO> misArticulos(Integer clienteId) {

        List<EstadoArticulo> estados =
                estadoArticuloRepository.findByClientePublicador(clienteId);

        List<ArticuloDTO> resultado = new ArrayList<>();
        for (EstadoArticulo estado : estados) {

            Producto producto = productoRepository.findById(estado.getProducto())
                    .orElse(null);
            if (producto == null) continue;

            ArticuloDTO dto = new ArticuloDTO();
            dto.setProductoId(producto.getIdentificador());
            dto.setNombre(producto.getDescripcionCatalogo());
            dto.setDescripcionCompleta(producto.getDescripcionCompleta());
            dto.setEstado(estado.getEstado());
            dto.setPrecioPropuesto(estado.getPrecioPropuesto());
            dto.setComisionPropuesta(estado.getComisionPropuesta());
            dto.setMotivoRechazo(estado.getMotivoRechazo());

            // Primera foto para la card
            List<Foto> fotos = fotoRepository.findByProducto(producto.getIdentificador());
            if (!fotos.isEmpty()) {
                String base64 = Base64.getEncoder().encodeToString(fotos.get(0).getFoto());
                dto.setFotoPrincipal("data:image/jpeg;base64," + base64);
            }

            resultado.add(dto);
        }
        return resultado;
    }

    // ─────────────────────────────────────────────
    // DETALLE de un artículo (con todas las fotos)
    // ─────────────────────────────────────────────
    public ArticuloDetalleDTO obtenerDetalle(Integer productoId, Integer clienteId) {

        EstadoArticulo estado = estadoArticuloRepository.findByProducto(productoId)
                .orElseThrow(() -> new BusinessException("Artículo no encontrado"));

        // Validar que el artículo pertenezca al usuario
        if (!estado.getClientePublicador().equals(clienteId)) {
            throw new BusinessException("Este artículo no te pertenece");
        }

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));

        ArticuloDetalleDTO dto = new ArticuloDetalleDTO();
        dto.setProductoId(producto.getIdentificador());
        dto.setNombre(producto.getDescripcionCatalogo());
        dto.setDescripcionCompleta(producto.getDescripcionCompleta());
        dto.setEstado(estado.getEstado());
        dto.setPrecioPropuesto(estado.getPrecioPropuesto());
        dto.setComisionPropuesta(estado.getComisionPropuesta());
        dto.setMotivoRechazo(estado.getMotivoRechazo());

        // Todas las fotos
        List<Foto> fotos = fotoRepository.findByProducto(productoId);
        List<String> fotosBase64 = new ArrayList<>();
        for (Foto f : fotos) {
            String base64 = Base64.getEncoder().encodeToString(f.getFoto());
            fotosBase64.add("data:image/jpeg;base64," + base64);
        }
        dto.setFotos(fotosBase64);

        return dto;
    }

    // ─────────────────────────────────────────────
    // ACEPTAR o RECHAZAR las condiciones (precio propuesto por la empresa)
    // ─────────────────────────────────────────────
    @Transactional
    public ArticuloResponse responderCondiciones(Integer productoId, Integer clienteId,
                                                 AceptarCondicionesRequest request) {

        EstadoArticulo estado = estadoArticuloRepository.findByProducto(productoId)
                .orElseThrow(() -> new BusinessException("Artículo no encontrado"));

        // Validar pertenencia
        if (!estado.getClientePublicador().equals(clienteId)) {
            throw new BusinessException("Este artículo no te pertenece");
        }

        // Solo se puede responder si la empresa ya propuso un precio
        if (!"PRECIO_ASIGNADO".equals(estado.getEstado())) {
            throw new BusinessException(
                "El artículo no está en estado de aceptar condiciones");
        }

        if (Boolean.TRUE.equals(request.getAcepta())) {
            estado.setEstado("ACEPTADO");
            estadoArticuloRepository.save(estado);
            return new ArticuloResponse(productoId, "ACEPTADO",
                "Aceptaste las condiciones. La empresa asignará tu artículo a una subasta.");
        } else {
            estado.setEstado("CANCELADO");
            estadoArticuloRepository.save(estado);
            return new ArticuloResponse(productoId, "CANCELADO",
                "Rechazaste las condiciones. El artículo no se subastará.");
        }
    }

    // El usuario confirma que envió el producto al depósito
    @Transactional
    public ArticuloResponse confirmarEnvio(Integer productoId, Integer clienteId) {

        EstadoArticulo estado = estadoArticuloRepository.findByProducto(productoId)
                .orElseThrow(() -> new BusinessException("Artículo no encontrado"));

        // Validar pertenencia
        if (!estado.getClientePublicador().equals(clienteId)) {
            throw new BusinessException("Este artículo no te pertenece");
        }

        // Solo se puede confirmar envío si está pendiente de envío
        if (!"PENDIENTE_ENVIO".equals(estado.getEstado())) {
            throw new BusinessException(
                "El artículo no está pendiente de envío");
        }

        estado.setEstado("ENVIADO");
        estadoArticuloRepository.save(estado);

        return new ArticuloResponse(productoId, "ENVIADO",
            "Confirmamos el envío. Te avisaremos cuando inspeccionemos el artículo.");
    }
}