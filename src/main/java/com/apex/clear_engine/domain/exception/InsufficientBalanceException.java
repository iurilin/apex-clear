package com.apex.clear_engine.domain.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
  public InsufficientBalanceException(String accountNumber, BigDecimal requested, BigDecimal available) {
    super(String.format("Saldo insuficiente na conta %s. Valor solicitado: R$ %.2f. Saldo disponível: R$ %.2f.",
            accountNumber, requested, available));
  }
}
