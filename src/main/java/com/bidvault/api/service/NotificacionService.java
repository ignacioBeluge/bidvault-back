package com.bidvault.api.service;

import com.bidvault.api.dto.notificacion.NotificacionDTO;
import com.bidvault.api.entity.Notificacion;
import com.bidvault.api.repository.NotificacionRepository;
import com.bidvault.api.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    // Lista las notificaciones del usuario (más nuevas primero)
    public List<NotificacionDTO> listar(Integer personaId) {
        List<Notificacion> notifs =
                notificacionRepository.findByPersonaOrderByFechaCreacionDesc(personaId);

        List<NotificacionDTO> resultado = new ArrayList<>();
        for (Notificacion n : notifs) {
            NotificacionDTO dto = new NotificacionDTO();
            dto.setId(n.getIdentificador());
            dto.setTipo(n.getTipo());
            dto.setTitulo(n.getTitulo());
            dto.setMensaje(n.getMensaje());
            dto.setLeida("si".equals(n.getLeida()));
            dto.setDestinoNavegacion(n.getDestinoNavegacion());
            dto.setParametrosNavegacion(n.getParametrosNavegacion());
            dto.setFechaCreacion(n.getFechaCreacion());
            resultado.add(dto);
        }
        return resultado;
    }

    // Cuenta las no leídas (para el badge de la campanita)
    public long contarNoLeidas(Integer personaId) {
        return notificacionRepository.countByPersonaAndLeida(personaId, "no");
    }

    // Marca una notificación como leída
    @Transactional
    public void marcarLeida(Integer notifId, Integer personaId) {
        Notificacion notif = notificacionRepository.findById(notifId)
                .orElseThrow(() -> new BusinessException("Notificación no encontrada"));

        // Validar que la notificación sea del usuario
        if (!notif.getPersona().equals(personaId)) {
            throw new BusinessException("Esta notificación no te pertenece");
        }

        notif.setLeida("si");
        notificacionRepository.save(notif);
    }

    // Marca todas como leídas
    @Transactional
    public void marcarTodasLeidas(Integer personaId) {
        List<Notificacion> notifs =
                notificacionRepository.findByPersonaOrderByFechaCreacionDesc(personaId);
        for (Notificacion n : notifs) {
            if ("no".equals(n.getLeida())) {
                n.setLeida("si");
                notificacionRepository.save(n);
            }
        }
    }
}