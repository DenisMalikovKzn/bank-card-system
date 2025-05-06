package com.example.demo.dto;

import com.example.demo.enums.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponse(Long id,
                           String maskedNumber,
                           String ownerEmail,
                           LocalDate expirationDate,
                           CardStatus status,
                           BigDecimal balance
) {}
