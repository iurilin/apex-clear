package com.apex.clear_engine.infrastructure.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank(message = "Conta de origem é obrigatória")
        String sourceAccountNumber,

        @NotBlank(message = "Conta de destino é obrigatória")
        String destinationAccountNumber,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal amount
) {}
