package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(Long transactionId,
                               Long fromCardId,
                               Long toCardId,
                               BigDecimal amount,
                               LocalDateTime timestamp
) {}
