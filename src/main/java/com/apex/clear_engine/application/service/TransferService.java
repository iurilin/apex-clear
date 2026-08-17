package com.apex.clear_engine.application.service;

import com.apex.clear_engine.domain.exception.AccountNotFoundException;
import com.apex.clear_engine.domain.exception.InsufficientBalanceException;
import com.apex.clear_engine.domain.model.Account;
import com.apex.clear_engine.domain.model.TransactionLedger;
import com.apex.clear_engine.domain.model.TransactionType;
import com.apex.clear_engine.domain.repository.AccountRepository;
import com.apex.clear_engine.domain.repository.TransactionLedgerRepository;
import com.apex.clear_engine.infrastructure.configuration.RabbitMQConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionLedgerRepository ledgerRepository;
    private final RabbitTemplate rabbitTemplate;

    // Métricas personalizadas
    private final Counter transferSuccessCounter;
    private final Counter transferFailedCounter;

    public TransferService(AccountRepository accountRepository,
                           TransactionLedgerRepository ledgerRepository,
                           RabbitTemplate rabbitTemplate,
                           MeterRegistry meterRegistry) {
        this.accountRepository = accountRepository;
        this.ledgerRepository = ledgerRepository;
        this.rabbitTemplate = rabbitTemplate;

        // Registra os contadores no Prometheus
        this.transferSuccessCounter = Counter.builder("transfers.completed.total")
                .description("Total de transferencias executadas com sucesso")
                .tag("service", "transfer-engine")
                .register(meterRegistry);

        this.transferFailedCounter = Counter.builder("transfers.failed.total")
                .description("Total de transferencias com falha")
                .tag("service", "transfer-engine")
                .register(meterRegistry);
    }

    @Caching(evict = {
            @CacheEvict(value = "accounts", key = "#sourceAccountNumber"),
            @CacheEvict(value = "accounts", key = "#destinationAccountNumber")
    })
    @Transactional
    public UUID executeTransfer(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount) {
        try {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("O valor da transferência deve ser maior que zero.");
            }

            if (sourceAccountNumber.equals(destinationAccountNumber)) {
                throw new IllegalArgumentException("A conta de origem não pode ser igual à conta de destino.");
            }

            Account sourceAccount = accountRepository.findByAccountNumberForUpdate(sourceAccountNumber)
                    .orElseThrow(() -> new AccountNotFoundException(sourceAccountNumber));

            Account destinationAccount = accountRepository.findByAccountNumberForUpdate(destinationAccountNumber)
                    .orElseThrow(() -> new AccountNotFoundException(destinationAccountNumber));

            if (sourceAccount.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException(sourceAccountNumber, amount, sourceAccount.getBalance());
            }

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

            String mensagemTexto = String.format(
                    "id:%s,origem:%s,destino:%s,valor:%s",
                    correlationId,
                    sourceAccount.getAccountNumber(),
                    destinationAccount.getAccountNumber(),
                    amount.toString()
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    mensagemTexto
            );

            // Incrementa métrica de sucesso
            transferSuccessCounter.increment();

            return correlationId;

        } catch (Exception e) {
            transferFailedCounter.increment();
            throw e;
        }
    }
}