package com.wallet.ewallet.repository;

import com.wallet.ewallet.entity.Transaction;
import com.wallet.ewallet.entity.TransactionType;
import com.wallet.ewallet.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    @Query("""
SELECT FUNCTION('DATE', t.createdAt), SUM(t.amount)
FROM Transaction t
WHERE t.status = 'SUCCESS'
GROUP BY FUNCTION('DATE', t.createdAt)
ORDER BY FUNCTION('DATE', t.createdAt)
""")
    List<Object[]> getDailyRevenue();

    @Query("""
SELECT FUNCTION('TO_CHAR', t.createdAt, 'YYYY-MM'), SUM(t.amount)
FROM Transaction t
WHERE t.status = 'SUCCESS'
GROUP BY FUNCTION('TO_CHAR', t.createdAt, 'YYYY-MM')
ORDER BY FUNCTION('TO_CHAR', t.createdAt, 'YYYY-MM')
""")
    List<Object[]> getMonthlyRevenue();

    @Query("""
    SELECT t.type, SUM(t.amount)
    FROM Transaction t
    WHERE t.status = 'SUCCESS'
    GROUP BY t.type
""")
    List<Object[]> getAmountByType();

    @Query("""
    SELECT t.sender.email, SUM(t.amount)
    FROM Transaction t
    WHERE t.status = 'SUCCESS'
    GROUP BY t.sender.email
    ORDER BY SUM(t.amount) DESC
""")
    List<Object[]> getTopUsers(Pageable pageable);

    // ===== USER REPORT =====

    @Query("""
SELECT t FROM Transaction t
WHERE (t.sender.id = :userId OR t.receiver.id = :userId)
AND t.status = 'SUCCESS'
""")
    List<Transaction> findAllByUser(UUID userId);

    @Query("""
SELECT t FROM Transaction t
WHERE (t.sender.id = :userId OR t.receiver.id = :userId)
AND t.status = 'SUCCESS'
AND t.createdAt BETWEEN :start AND :end
""")
    List<Transaction> findByUserAndTime(
            UUID userId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
SELECT t FROM Transaction t
WHERE (t.sender.id = :userId OR t.receiver.id = :userId)
AND t.status = 'SUCCESS'
ORDER BY t.amount DESC
""")
    List<Transaction> findTopTransactions(UUID userId, Pageable pageable);

    @Query("""
SELECT t FROM Transaction t
WHERE (t.sender.id = :userId OR t.receiver.id = :userId)
AND t.status = 'SUCCESS'
ORDER BY t.createdAt DESC
""")
    List<Transaction> findLatestTransactions(UUID userId, Pageable pageable);

    @Query("""
SELECT FUNCTION('TO_CHAR', t.createdAt, 'YYYY-MM'), SUM(t.amount)
FROM Transaction t
WHERE t.status = 'SUCCESS'
AND (
    (t.sender IS NOT NULL AND t.sender.id = :userId)
    OR
    (t.receiver IS NOT NULL AND t.receiver.id = :userId)
)
GROUP BY FUNCTION('TO_CHAR', t.createdAt, 'YYYY-MM')
ORDER BY FUNCTION('TO_CHAR', t.createdAt, 'YYYY-MM')
""")
    List<Object[]> getMonthlyTrend(UUID userId);

    @Query("""
SELECT FUNCTION('TO_CHAR', t.createdAt, 'YYYY'), SUM(t.amount)
FROM Transaction t
WHERE t.status = 'SUCCESS'
AND (
    (t.sender IS NOT NULL AND t.sender.id = :userId)
    OR
    (t.receiver IS NOT NULL AND t.receiver.id = :userId)
)
GROUP BY FUNCTION('TO_CHAR', t.createdAt, 'YYYY')
ORDER BY FUNCTION('TO_CHAR', t.createdAt, 'YYYY')
""")
    List<Object[]> getYearlyTrend(UUID userId);

    @Query("""
SELECT t FROM Transaction t
WHERE (t.sender.id = :userId OR t.receiver.id = :userId)
AND (:type IS NULL OR t.type = :type)
AND (:start IS NULL OR t.createdAt >= :start)
AND (:end IS NULL OR t.createdAt <= :end)
AND (:minAmount IS NULL OR t.amount >= :minAmount)
AND (:maxAmount IS NULL OR t.amount <= :maxAmount)
ORDER BY t.createdAt DESC
""")
    Page<Transaction> filterTransactions(
            UUID userId,
            TransactionType type,
            LocalDateTime start,
            LocalDateTime end,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    );
}