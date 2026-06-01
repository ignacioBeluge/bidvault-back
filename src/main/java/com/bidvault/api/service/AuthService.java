package com.bidvault.api.service;

import com.bidvault.api.dto.auth.LoginRequest;
import com.bidvault.api.dto.auth.LoginResponse;
import com.bidvault.api.entity.Credencial;
import com.bidvault.api.entity.Cliente;
import com.bidvault.api.entity.Persona;
import com.bidvault.api.repository.CredencialRepository;
import com.bidvault.api.repository.ClienteRepository;
import com.bidvault.api.repository.PersonaRepository;
import com.bidvault.api.security.JwtService;
import com.bidvault.api.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor   // Lombok: inyecta los campos final por constructor
public class AuthService {

    private final CredencialRepository credencialRepository;
    private final ClienteRepository clienteRepository;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;    // lo creamos en el paso de seguridad

    public LoginResponse login(LoginRequest request) {

        // 1. Buscar la credencial por email
        Credencial credencial = credencialRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Email o contraseña incorrectos"));

        // 2. Verificar la contraseña (compara el hash, nunca texto plano)
        if (!passwordEncoder.matches(request.getPassword(), credencial.getPasswordHash())) {
            throw new BusinessException("Email o contraseña incorrectos");
        }

        // 3. Traer los datos de persona y cliente
        Persona persona = personaRepository.findById(credencial.getPersona())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        Cliente cliente = clienteRepository.findById(credencial.getPersona())
                .orElseThrow(() -> new BusinessException("El usuario no es un cliente"));

        // 4. Validar que la cuenta esté admitida por la empresa
        if (!"si".equals(cliente.getAdmitido())) {
            throw new BusinessException("Tu cuenta aún no fue aprobada por la empresa");
        }

        // 5. Generar el token JWT
        String token = jwtService.generarToken(credencial.getPersona(), persona.getNombre());

        // 6. Armar la respuesta
        return new LoginResponse(
                token,
                credencial.getPersona(),
                persona.getNombre(),
                cliente.getCategoria(),
                credencial.getEtapaRegistro()
        );
    }
}