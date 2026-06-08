package com.bidvault.api.service;

import com.bidvault.api.dto.MedioPagoDTO;
import com.bidvault.api.dto.MedioPagoRequest;
import com.bidvault.api.entity.ChequeCertificado;
import com.bidvault.api.entity.CuentaBancaria;
import com.bidvault.api.entity.MedioDePago;
import com.bidvault.api.entity.Tarjeta;
import com.bidvault.api.repository.ChequeCertificadoRepository;
import com.bidvault.api.repository.CuentaBancariaRepository;
import com.bidvault.api.repository.MedioDePagoRepository;
import com.bidvault.api.repository.TarjetaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedioPagoService {

    private final MedioDePagoRepository medioDePagoRepository;
    private final TarjetaRepository tarjetaRepository;
    private final CuentaBancariaRepository cuentaBancariaRepository;
    private final ChequeCertificadoRepository chequeCertificadoRepository;


    // Lista los medios de pago de un cliente
    public List<MedioPagoDTO> listarPorCliente(Integer clienteId) {
        List<MedioDePago> medios = medioDePagoRepository.findByCliente(clienteId);
        List<MedioPagoDTO> resultado = new ArrayList<>();

        for (MedioDePago m : medios) {
            MedioPagoDTO dto = new MedioPagoDTO();
            dto.setId(m.getIdentificador());
            dto.setTipo(m.getTipo());
            dto.setVerificado(m.getVerificado());
            dto.setEsPrincipal(m.getEsPrincipal());
            resultado.add(dto);
        }
        return resultado;
    }

    // Registra un nuevo medio de pago para el cliente
    @Transactional
    public MedioPagoDTO crear(Integer clienteId, MedioPagoRequest request) {

        // 1. Crear el medio de pago base
        MedioDePago medio = new MedioDePago();
        medio.setCliente(clienteId);
        medio.setTipo(request.getTipo());
        medio.setEsPrincipal("no");
        // En un sistema real arranca 'no' y la empresa lo verifica.
        // Para la demo lo dejamos verificado directo así puede pujar.
        medio.setVerificado("no");
        medio.setFechaRegistro(LocalDateTime.now());
        medio = medioDePagoRepository.save(medio);

        // 2. Si es tarjeta, guardar el detalle
        if ("tarjeta".equals(request.getTipo())) {
            Tarjeta tarjeta = new Tarjeta();
            tarjeta.setMedioPago(medio.getIdentificador());
            // Solo guardamos los últimos 4 dígitos por seguridad
            String num = request.getNumeroTarjeta();
            tarjeta.setUltimosCuatroDigitos(num.substring(num.length() - 4));
            tarjeta.setMarca(request.getMarca());
            tarjeta.setTitular(request.getTitular());
            tarjeta.setFechaVencimiento(request.getVencimiento());
            tarjeta.setEsInternacional(
                Boolean.TRUE.equals(request.getEsInternacional()) ? "si" : "no");
            tarjeta.setMoneda(request.getMoneda() != null ? request.getMoneda() : "ARS");
            tarjetaRepository.save(tarjeta);
        }

        // Si es cuenta bancaria, guardar el detalle
        if ("cuenta_bancaria".equals(request.getTipo())) {
            CuentaBancaria cuenta = new CuentaBancaria();
            cuenta.setMedioPago(medio.getIdentificador());
            cuenta.setBanco(request.getBanco());
            cuenta.setNumeroCuenta(request.getCbu());   // usamos el CBU como nro de cuenta
            cuenta.setCbu(request.getCbu());
            cuenta.setAlias(request.getAlias());
            cuenta.setTitular(request.getTitular());
            cuenta.setEsExtranjera(
                Boolean.TRUE.equals(request.getEsInternacional()) ? "si" : "no");
            cuenta.setMoneda(request.getMoneda() != null ? request.getMoneda() : "ARS");
            cuentaBancariaRepository.save(cuenta);
        }

        // Si es cheque certificado, guardar el detalle
        if ("cheque_certificado".equals(request.getTipo())) {
            ChequeCertificado cheque = new ChequeCertificado();
            cheque.setMedioPago(medio.getIdentificador());
            cheque.setBancoEmisor(request.getBancoEmisor());
            cheque.setNumeroCheque(request.getNumeroCheque());
            cheque.setFechaEmision(java.time.LocalDate.now());
            cheque.setFechaVencimiento(java.time.LocalDate.now().plusMonths(3));
            cheque.setMonto(request.getMonto());
            chequeCertificadoRepository.save(cheque);
        }

        // 3. Devolver el DTO
        MedioPagoDTO dto = new MedioPagoDTO();
        dto.setId(medio.getIdentificador());
        dto.setTipo(medio.getTipo());
        dto.setVerificado(medio.getVerificado());
        dto.setEsPrincipal(medio.getEsPrincipal());
        return dto;
    }
}
