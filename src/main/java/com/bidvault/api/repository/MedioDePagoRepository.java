package com.bidvault.api.repository;

import com.bidvault.api.entity.MedioDePago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedioDePagoRepository extends JpaRepository<MedioDePago, Integer> {

    // Todos los medios de pago de un cliente.
    List<MedioDePago> findByCliente(Integer cliente);

    // Cuenta cuántos medios verificados tiene el cliente.
    // Si es > 0, puede pujar. Si es 0, solo puede ver la subasta.
    long countByClienteAndVerificado(Integer cliente, String verificado);
}