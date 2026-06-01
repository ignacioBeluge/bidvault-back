package com.bidvault.api.config;

import com.bidvault.api.entity.Credencial;
import com.bidvault.api.repository.CredencialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CredencialRepository credencialRepository;
    private final PasswordEncoder passwordEncoder;

    // Se ejecuta automáticamente al arrancar la app, una sola vez.
    @Override
    public void run(String... args) {
        credencialRepository.findByEmail("juan.garcia@email.com")
                .ifPresent(credencial -> {
                    // Genera el hash con TU versión de BCrypt y lo actualiza
                    String hashCorrecto = passwordEncoder.encode("123456");
                    credencial.setPasswordHash(hashCorrecto);
                    credencialRepository.save(credencial);
                    System.out.println("=== Contraseña del usuario de prueba actualizada ===");
                });
    }
}