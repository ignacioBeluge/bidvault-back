package com.bidvault.api.service;

import com.bidvault.api.dto.auth.*;
import com.bidvault.api.entity.*;
import com.bidvault.api.repository.*;
import com.bidvault.api.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RegistroService {

    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final CredencialRepository credencialRepository;
    private final FotoDocumentoRepository fotoDocumentoRepository;
    private final PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────────
    // ETAPA 1 — Crear cuenta pendiente de aprobación
    // ─────────────────────────────────────────────
    @Transactional
    public RegistroPaso1Response registrarPaso1(RegistroPaso1Request request) {

        // 1. Validar que el email no esté ya registrado
        if (credencialRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Ya existe una cuenta con ese email");
        }

        // 2. Crear la persona
        Persona persona = new Persona();
        persona.setNombre(request.getNombre());
        persona.setDocumento(request.getDocumento());
        persona.setDireccion(request.getDireccion());
        persona.setEstado("activo");
        persona = personaRepository.save(persona);

        // 3. Crear el cliente vinculado a la persona
        //    admitido='no' → pendiente de aprobación del empleado
        //    categoria='comun' → se le asigna la mínima hasta que el empleado decida
        Cliente cliente = new Cliente();
        cliente.setPersona(persona);          // por @MapsId, comparte el id
        cliente.setNumeroPais(request.getNumeroPais());
        cliente.setAdmitido("no");
        cliente.setCategoria("comun");
        cliente.setVerificador(1);            // empleado verificador por defecto
        clienteRepository.save(cliente);

        // 4. Crear la credencial SIN contraseña todavía (etapa 1)
        Credencial credencial = new Credencial();
        credencial.setPersona(persona.getIdentificador());
        credencial.setEmail(request.getEmail());
        credencial.setPasswordHash("PENDIENTE");   // placeholder, se setea en etapa 2
        credencial.setEtapaRegistro(1);
        credencial.setFechaCreacion(LocalDateTime.now());
        credencialRepository.save(credencial);

        // 5. Guardar las fotos del DNI (si vinieron)
        guardarFotoDni(persona.getIdentificador(), "frente", request.getFotoDniFrente());
        guardarFotoDni(persona.getIdentificador(), "dorso", request.getFotoDniDorso());

        return new RegistroPaso1Response(
                persona.getIdentificador(),
                "Tu cuenta fue creada y está en revisión. Te avisaremos cuando sea aprobada.",
                1
        );
    }

    // Consulta en qué etapa está el registro y si ya fue aprobado
    public RegistroEstadoDTO consultarEstado(Integer usuarioId) {
        Credencial credencial = credencialRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        Cliente cliente = clienteRepository.findById(credencial.getPersona())
                .orElseThrow(() -> new BusinessException("Cliente no encontrado"));

        boolean aprobado = "si".equals(cliente.getAdmitido());
        return new RegistroEstadoDTO(
            usuarioId,
            credencial.getEtapaRegistro(),
            aprobado,
            cliente.getCategoria()
        );
    }

    // ─────────────────────────────────────────────
    // ETAPA 2 — Activar cuenta con contraseña (requiere estar aprobado)
    // ─────────────────────────────────────────────
    @Transactional
    public LoginResponse registrarPaso2(RegistroPaso2Request request) {

        // 1. Validar que las contraseñas coincidan
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new BusinessException("Las contraseñas no coinciden");
        }

        // 2. Buscar la credencial del usuario
        Credencial credencial = credencialRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        // Nota: buscamos por el id de credencial. Si preferís por persona,
        // habría que agregar un findByPersona al repository.

        // 3. Validar que la cuenta haya sido aprobada por la empresa
        Cliente cliente = clienteRepository.findById(credencial.getPersona())
                .orElseThrow(() -> new BusinessException("Cliente no encontrado"));

        if (!"si".equals(cliente.getAdmitido())) {
            throw new BusinessException(
                "Tu cuenta todavía no fue aprobada. Esperá la confirmación de la empresa.");
        }

        // 4. Setear la contraseña (hasheada) y pasar a etapa 2
        credencial.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        credencial.setEtapaRegistro(2);
        credencialRepository.save(credencial);

        // 5. Traer datos para devolver (similar al login)
        Persona persona = personaRepository.findById(credencial.getPersona())
                .orElseThrow(() -> new BusinessException("Persona no encontrada"));

        // Devolvemos sin token — el usuario hace login después.
        // Si querés auto-login, acá generarías el JWT.
        return new LoginResponse(
                null,    // sin token (que haga login)
                persona.getIdentificador(),
                persona.getNombre(),
                cliente.getCategoria(),
                2
        );
    }

    // Helper para guardar una foto del DNI desde base64
    private void guardarFotoDni(Integer personaId, String tipo, String base64) {
        if (base64 == null || base64.isBlank()) return;
        try {
            FotoDocumento foto = new FotoDocumento();
            foto.setPersona(personaId);
            foto.setTipo(tipo);
            foto.setFoto(Base64.getDecoder().decode(base64));
            foto.setFechaCarga(LocalDateTime.now());
            fotoDocumentoRepository.save(foto);
        } catch (IllegalArgumentException e) {
            // Si el base64 es inválido, lo ignoramos (no rompe el registro)
        }
    }
}