package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
    @NotNull Long fromCardId,
    @NotNull Long toCardId,
    @Positive @DecimalMin(value = "0.01", message = "Amount must be at least 0.01") BigDecimal amount
){}
