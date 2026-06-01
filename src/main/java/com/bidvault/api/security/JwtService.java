package com.bidvault.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    // Clave secreta para firmar los tokens. En producción va en variable de entorno.
    // Debe tener al menos 32 caracteres para HS256.
    private static final String SECRET =
            "bidvault-clave-secreta-super-larga-para-firmar-jwt-2026";

    private static final long EXPIRACION_MS = 1000 * 60 * 60 * 24; // 24 horas

    private SecretKey getClave() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    // Genera un token con el id del usuario y su nombre.
    public String generarToken(Integer usuarioId, String nombre) {
        return Jwts.builder()
                .subject(String.valueOf(usuarioId))   // el "subject" es el id del usuario
                .claim("nombre", nombre)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRACION_MS))
                .signWith(getClave())
                .compact();
    }

    // Extrae el id del usuario desde el token.
    public Integer extraerUsuarioId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getClave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Integer.valueOf(claims.getSubject());
    }

    // Valida que el token no esté expirado ni manipulado.
    public boolean esValido(String token) {
        try {
            Jwts.parser()
                .verifyWith(getClave())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
