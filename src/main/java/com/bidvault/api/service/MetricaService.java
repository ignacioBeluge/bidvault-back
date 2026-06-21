package com.bidvault.api.service;

import com.bidvault.api.dto.metrica.MetricasDTO;
import com.bidvault.api.entity.*;
import com.bidvault.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetricaService {

    private final AsistenteRepository asistenteRepository;
    private final PujoRepository pujoRepository;
    private final PagoVentaRepository pagoVentaRepository;
    private final MultaRepository multaRepository;

    public MetricasDTO obtenerMetricas(Integer clienteId) {

        MetricasDTO dto = new MetricasDTO();

        // 1. Subastas a las que asistió (una entrada por subasta)
        List<Asistente> asistentes = asistenteRepository.findByCliente(clienteId);
        dto.setSubastasParticipadas(asistentes.size());

        // 2. Recorrer todas sus pujas: total ofertado, cantidad y veces que ganó
        int totalPujas = 0;
        int vecesGano = 0;
        BigDecimal totalOfertado = BigDecimal.ZERO;

        for (Asistente asistente : asistentes) {
            List<Pujo> pujos = pujoRepository.findByAsistente(asistente.getIdentificador());
            totalPujas += pujos.size();

            for (Pujo pujo : pujos) {
                totalOfertado = totalOfertado.add(pujo.getImporte());
                if ("si".equals(pujo.getGanador())) {
                    vecesGano++;
                }
            }
        }

        dto.setTotalPujasRealizadas(totalPujas);
        dto.setVecesGano(vecesGano);
        dto.setTotalOfertado(totalOfertado);

        // 3. Total pagado (de las compras que ya pagó)
        List<PagoVenta> pagos = pagoVentaRepository.findByCliente(clienteId);
        BigDecimal totalPagado = BigDecimal.ZERO;
        int pendientes = 0;
        for (PagoVenta pago : pagos) {
            if ("PAGADO".equals(pago.getEstado())) {
                totalPagado = totalPagado.add(pago.getMontoTotal());
            } else if ("PENDIENTE".equals(pago.getEstado())) {
                pendientes++;
            }
        }
        dto.setTotalPagado(totalPagado);
        dto.setComprasPendientes(pendientes);

        // 4. Cantidad de multas recibidas
        List<Multa> multas = multaRepository.findByCliente(clienteId);
        dto.setCantidadMultas(multas.size());

        return dto;
    }
}