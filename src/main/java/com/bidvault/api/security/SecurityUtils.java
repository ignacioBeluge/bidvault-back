package com.bidvault.api.security;

import org.springframework.security.core.context.SecurityContextHolder;

// Clase utilitaria para sacar el id del usuario logueado desde el token.
public class SecurityUtils {

    public static Integer getUsuarioId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return (Integer) principal;
    }
}