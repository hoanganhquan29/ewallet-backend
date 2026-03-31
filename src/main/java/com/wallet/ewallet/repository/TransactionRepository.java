package com.wallet.ewallet.repository;

import com.wallet.ewallet.entity.Transaction;
import com.wallet.ewallet.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransactionRepository extends
        JpaRepository<Transaction, UUID>,
        JpaSpecificationExecutor<Transaction> {

    Page<Transaction> findBySenderOrReceiver(
            User sender,
            User receiver,
            Pageable pageable
    );
    Page<Transaction> findBySuspiciousTrue(Pageable pageable);
    @Query("""
SELECT COALESCE(SUM(t.amount),0)
FROM Transaction t
WHERE t.sender.id = :userId
AND t.type = com.wallet.ewallet.entity.TransactionType.TRANSFER
AND t.createdAt >= :startOfDay
""")
    BigDecimal getTodayTransferAmount(
            UUID userId,
            LocalDateTime startOfDay
    );
    @Query("SELECT COUNT(t) FROM Transaction t")
    long countAllTransactions();

    @Query("SELECT COALESCE(SUM(t.amount),0) FROM Transaction t WHERE t.status = 'SUCCESS'")
    double sumAllAmount();

    @Query("SELECT COALESCE(SUM(t.amount),0) FROM Transaction t WHERE t.type = 'DEPOSIT' AND t.status = 'SUCCESS'")
    double sumDeposit();

    @Query("SELECT COALESCE(SUM(t.amount),0) FROM Transaction t WHERE t.type = 'TRANSFER' AND t.status = 'SUCCESS'")
    double sumTransfer();
}