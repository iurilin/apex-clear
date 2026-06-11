package com.apex.clear_engine.infrastructure.controller;

import com.apex.clear_engine.application.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> transfer(@RequestBody @Valid TransferRequest request) {
        UUID transactionId = transferService.executeTransfer(
                request.sourceAccountNumber(),
                request.destinationAccountNumber(),
                request.amount()
        );

        return ResponseEntity.ok(Map.of(
                "message", "Transferência processada com sucesso no core!",
                "transactionId", transactionId
        ));
    }
}
