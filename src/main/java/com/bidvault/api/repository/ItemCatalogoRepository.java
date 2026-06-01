package com.bidvault.api.repository;

import com.bidvault.api.entity.ItemCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemCatalogoRepository extends JpaRepository<ItemCatalogo, Integer> {

    // Trae todos los ítems de un catálogo.
    List<ItemCatalogo> findByCatalogo(Integer catalogoId);

    // Trae solo los ítems que todavía no fueron subastados.
    List<ItemCatalogo> findByCatalogoAndSubastado(Integer catalogoId, String subastado);
}