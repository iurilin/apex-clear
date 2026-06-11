package com.apex.clear_engine.application.service;

import com.apex.clear_engine.domain.model.Account;
import com.apex.clear_engine.domain.model.TransactionLedger;
import com.apex.clear_engine.domain.model.TransactionType;
import com.apex.clear_engine.domain.repository.AccountRepository;
import com.apex.clear_engine.domain.repository.TransactionLedgerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionLedgerRepository ledgerRepository;

    public TransferService(AccountRepository accountRepository, TransactionLedgerRepository ledgerRepository) {
        this.accountRepository = accountRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @Transactional
    public UUID executeTransfer(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transferência deve ser maior que zero.");
        }

        if (sourceAccountNumber.equals(destinationAccountNumber)) {
            throw new IllegalArgumentException("A conta de origem não pode ser igual à conta de destino.");
        }

        Account sourceAccount = accountRepository.findByAccountNumberForUpdate(sourceAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada: " + sourceAccountNumber));

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Saldo insuficiente para realizar a transação.");
        }

        Account destinationAccount = accountRepository.findByAccountNumberForUpdate(destinationAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Conta de destino não encontrada: " + destinationAccountNumber));

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        UUID correlationId = UUID.randomUUID();

        TransactionLedger debitEntry = TransactionLedger.builder()
                .correlationId(correlationId)
                .account(sourceAccount)
                .type(TransactionType.DEBIT)
                .amount(amount)
                .description("Transferência enviada para conta " + destinationAccountNumber)
                .build();

        TransactionLedger creditEntry = TransactionLedger.builder()
                .correlationId(correlationId)
                .account(destinationAccount)
                .type(TransactionType.CREDIT)
                .amount(amount)
                .description("Transferência recebida da conta " + sourceAccountNumber)
                .build();

        ledgerRepository.save(debitEntry);
        ledgerRepository.save(creditEntry);

        return correlationId;
    }
}