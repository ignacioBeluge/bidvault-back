package com.bidvault.api.repository;

import com.bidvault.api.entity.EstadoArticulo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EstadoArticuloRepository extends JpaRepository<EstadoArticulo, Integer> {

    // Todos los artículos publicados por un cliente (para "Mis Artículos")
    List<EstadoArticulo> findByClientePublicador(Integer clientePublicador);

    // El estado de un producto específico
    Optional<EstadoArticulo> findByProducto(Integer producto);

    boolean existsByCuentaCobro(Integer cuentaCobro);
}
