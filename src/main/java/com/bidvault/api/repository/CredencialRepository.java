package com.bidvault.api.repository;

import com.bidvault.api.entity.Credencial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CredencialRepository extends JpaRepository<Credencial, Integer> {

    // Spring Data genera la query sola a partir del nombre del método.
    // findByEmail → SELECT * FROM credenciales WHERE email = ?
    // Lo usamos en el login para buscar al usuario por su email.
    Optional<Credencial> findByEmail(String email);

    // Para validar en el registro que el email no esté repetido.
    boolean existsByEmail(String email);

    // En CredencialRepository, agregá:
    Optional<Credencial> findByPersona(Integer persona);

}
