package com.bidvault.api.repository;

import com.bidvault.api.entity.PagoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PagoVentaRepository extends JpaRepository<PagoVenta, Integer> {

    // Las compras de un cliente (las más nuevas primero las ordenamos en el service)
    List<PagoVenta> findByCliente(Integer cliente);
}