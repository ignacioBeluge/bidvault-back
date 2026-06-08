package com.bidvault.api.service;

import com.bidvault.api.dto.subasta.*;
import com.bidvault.api.entity.*;
import com.bidvault.api.repository.*;
import com.bidvault.api.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubastaService {

    private final SubastaRepository subastaRepository;
    private final ClienteRepository clienteRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final ProductoRepository productoRepository;
    private final PujoRepository pujoRepository;
    private final CatalogoRepository catalogoRepository; // ver nota al final
    private final FotoRepository fotoRepository;

    // Orden jerárquico de categorías para comparar
    private static final List<String> JERARQUIA =
            List.of("comun", "especial", "plata", "oro", "platino");

    // Lista las subastas abiertas que el usuario puede ver según su categoría.
    public List<SubastaResumenDTO> listarSubastas(Integer clienteId) {

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new BusinessException("Cliente no encontrado"));

        int nivelUsuario = JERARQUIA.indexOf(cliente.getCategoria());

        // Trae todas las abiertas y filtra las que el usuario puede ver:
        // la categoría de la subasta debe ser <= la del usuario.
        List<Subasta> subastas = subastaRepository.findByEstado("abierta");

        List<SubastaResumenDTO> resultado = new ArrayList<>();
        for (Subasta s : subastas) {
            int nivelSubasta = JERARQUIA.indexOf(s.getCategoria());
            if (nivelSubasta <= nivelUsuario) {
                resultado.add(mapearResumen(s));
            }
        }
        return resultado;
    }

    // Detalle de una subasta con su catálogo.
    public SubastaDetalleDTO obtenerDetalle(Integer subastaId, Integer clienteId) {

        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new BusinessException("Subasta no encontrada"));

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new BusinessException("Cliente no encontrado"));

        // Validar categoría
        int nivelUsuario = JERARQUIA.indexOf(cliente.getCategoria());
        int nivelSubasta = JERARQUIA.indexOf(subasta.getCategoria());
        if (nivelSubasta > nivelUsuario) {
            throw new BusinessException("Tu categoría no te permite acceder a esta subasta");
        }

        SubastaDetalleDTO dto = new SubastaDetalleDTO();
        dto.setId(subasta.getIdentificador());
        dto.setFecha(subasta.getFecha());
        dto.setHora(subasta.getHora());
        dto.setEstado(subasta.getEstado());
        dto.setCategoria(subasta.getCategoria());
        dto.setUbicacion(subasta.getUbicacion());
        dto.setCapacidadAsistentes(subasta.getCapacidadAsistentes());
        dto.setSubastador(subasta.getSubastador());

        // Traer los ítems del catálogo de esta subasta
        dto.setItems(obtenerItemsDeSubasta(subastaId));

        return dto;
    }

    // Trae los ítems del catálogo de una subasta con su mejor oferta actual.
    private List<ItemCatalogoDTO> obtenerItemsDeSubasta(Integer subastaId) {
        List<ItemCatalogoDTO> items = new ArrayList<>();

        // Buscar el catálogo de la subasta
        Catalogo catalogo = catalogoRepository.findBySubasta(subastaId)
                .orElse(null);
        if (catalogo == null) return items;

        List<ItemCatalogo> itemsCatalogo =
                itemCatalogoRepository.findByCatalogo(catalogo.getIdentificador());

        for (ItemCatalogo item : itemsCatalogo) {
            ItemCatalogoDTO dto = new ItemCatalogoDTO();
            dto.setId(item.getIdentificador());
            dto.setProducto(item.getProducto());
            dto.setPrecioBase(item.getPrecioBase());
            dto.setComision(item.getComision());
            dto.setSubastado(item.getSubastado());

            // Descripción del producto
            productoRepository.findById(item.getProducto()).ifPresent(p ->
                    dto.setDescripcion(p.getDescripcionCatalogo()));

            // Mejor oferta actual
            dto.setMejorOfertaActual(obtenerMejorOferta(item.getIdentificador()));

            items.add(dto);
        }
        return items;
    }

    // Devuelve la puja más alta de un ítem, o el precio base si no hay pujas.
    private BigDecimal obtenerMejorOferta(Integer itemId) {
        List<Pujo> pujas = pujoRepository.findByItemOrderByImporteDesc(itemId);
        if (pujas.isEmpty()) {
            return null; // no hay ofertas todavía
        }
        return pujas.get(0).getImporte();
    }

    private SubastaResumenDTO mapearResumen(Subasta s) {
        SubastaResumenDTO dto = new SubastaResumenDTO();
        dto.setId(s.getIdentificador());
        dto.setFecha(s.getFecha());
        dto.setHora(s.getHora());
        dto.setEstado(s.getEstado());
        dto.setCategoria(s.getCategoria());
        dto.setUbicacion(s.getUbicacion());
        return dto;
    }

    // Detalle de un ítem del catálogo con sus fotos
    public ItemDetalleDTO obtenerDetalleItem(Integer itemId) {

        ItemCatalogo item = itemCatalogoRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException("Ítem no encontrado"));

        Producto producto = productoRepository.findById(item.getProducto())
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));

        ItemDetalleDTO dto = new ItemDetalleDTO();
        dto.setId(item.getIdentificador());
        dto.setProducto(item.getProducto());
        dto.setDescripcionCatalogo(producto.getDescripcionCatalogo());
        dto.setDescripcionCompleta(producto.getDescripcionCompleta());
        dto.setPrecioBase(item.getPrecioBase());
        dto.setComision(item.getComision());
        dto.setSubastado(item.getSubastado());

        // Mejor oferta actual
        List<Pujo> pujas = pujoRepository.findByItemOrderByImporteDesc(itemId);
        dto.setMejorOfertaActual(pujas.isEmpty() ? null : pujas.get(0).getImporte());

        // Convertir las fotos binarias a base64 (data URI para mostrar directo)
        List<Foto> fotos = fotoRepository.findByProducto(item.getProducto());
        List<String> fotosBase64 = new ArrayList<>();
        for (Foto f : fotos) {
            String base64 = Base64.getEncoder().encodeToString(f.getFoto());
            fotosBase64.add("data:image/jpeg;base64," + base64);
        }
        dto.setFotos(fotosBase64);

        return dto;
    }


}