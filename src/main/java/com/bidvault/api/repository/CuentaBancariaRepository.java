package com.bidvault.api.repository;

import com.bidvault.api.entity.CuentaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaBancariaRepository extends JpaRepository<CuentaBancaria, Integer> {
}