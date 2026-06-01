package com.bidvault.api.repository;

import com.bidvault.api.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    // CRUD básico alcanza para traer el detalle del producto de un ítem.
}
