package com.apex.clear_engine.infrastructure.controller;

import com.apex.clear_engine.application.service.IdempotencyService;
import com.apex.clear_engine.application.service.TransferService;
import com.apex.clear_engine.domain.repository.AccountRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService transferService;
    private final IdempotencyService idempotencyService;
    private final AccountRepository accountRepository;

    public TransferController(TransferService transferService,
                              IdempotencyService idempotencyService,
                              AccountRepository accountRepository) {
        this.transferService = transferService;
        this.idempotencyService = idempotencyService;
        this.accountRepository = accountRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> transfer(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody @Valid TransferRequest request) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "O cabeçalho 'Idempotency-Key' é obrigatório para operações financeiras."));
        }

        boolean isUnique = idempotencyService.tryReserveKey(idempotencyKey);

        if (!isUnique) {
            String state = idempotencyService.getKeyState(idempotencyKey);

            if (state != null && state.startsWith("SUCCESS:")) {
                String savedTxId = state.replace("SUCCESS:", "");
                return ResponseEntity.ok(Map.of(
                        "message", "Requisição duplicada detectada. Retornando resultado original.",
                        "transactionId", savedTxId
                ));
            }

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Esta transação já está sendo processada. Por favor, aguarde."));
        }

        try {
            UUID transactionId = transferService.executeTransfer(
                    request.sourceAccountNumber(),
                    request.destinationAccountNumber(),
                    request.amount()
            );

            idempotencyService.confirmKey(idempotencyKey, transactionId.toString());

            return ResponseEntity.ok(Map.of(
                    "message", "Transferência processada com sucesso no core!",
                    "transactionId", transactionId
            ));

        } catch (Exception e) {
            idempotencyService.deleteKey(idempotencyKey);

            throw e;
        }
    }

    @GetMapping("/accounts/{accountNumber}/balance")
    public ResponseEntity<?> getBalance(@PathVariable String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> ResponseEntity.ok(Map.of(
                        "accountNumber", account.getAccountNumber(),
                        "balance", account.getBalance()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}