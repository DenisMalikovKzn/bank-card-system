package com.example.demo.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;

public record CardCreateRequest(@NotBlank String cardNumber,
                                @NotNull Long userId,
                                @Future LocalDate expirationDate
) {}
