package com.apex.clear_engine.domain.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String accountNumber) {
        super("A conta número '" + accountNumber + "' não foi encontrada no sistema.");
    }
}
