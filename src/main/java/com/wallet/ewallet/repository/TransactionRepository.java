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

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

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
AND t.type = 'TRANSFER'
AND t.createdAt >= :startOfDay
""")
    BigDecimal getTodayTransferAmount(
            UUID userId,
            LocalDateTime startOfDay
    );

}