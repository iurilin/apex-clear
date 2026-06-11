package com.apex.clear_engine.domain.repository;

import com.apex.clear_engine.domain.model.TransactionLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionLedgerRepository extends JpaRepository<TransactionLedger, UUID> {
}