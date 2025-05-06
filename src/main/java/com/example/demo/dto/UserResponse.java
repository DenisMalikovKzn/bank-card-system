package com.example.demo.dto;

import java.util.Set;

public record UserResponse(
        Long id,
        String email,
        Set<String> roles
) {}