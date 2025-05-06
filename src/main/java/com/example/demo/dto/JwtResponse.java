package com.example.demo.dto;

import java.util.Collection;

public record JwtResponse(
        String token,
        Long id,
        String email,
        Collection<? extends org.springframework.security.core.GrantedAuthority> roles
) {}