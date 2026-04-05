package com.wallet.ewallet.specification;

import com.wallet.ewallet.entity.Transaction;
import com.wallet.ewallet.entity.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionSpecification {

    public static Specification<Transaction> filter(
            UUID userId,
            TransactionType type,
            LocalDateTime start,
            LocalDateTime end,
            BigDecimal minAmount,
            BigDecimal maxAmount
    ) {
        return (root, query, cb) -> {

            // DEPOSIT có sender = null → phải check isNotNull trước khi so sánh id
            var senderMatch = cb.and(
                    root.get("sender").isNotNull(),
                    cb.equal(root.get("sender").get("id"), userId)
            );
            var receiverMatch = cb.and(
                    root.get("receiver").isNotNull(),
                    cb.equal(root.get("receiver").get("id"), userId)
            );

            var predicate = cb.or(senderMatch, receiverMatch);

            if (type != null) {
                predicate = cb.and(predicate, cb.equal(root.get("type"), type));
            }

            if (start != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }

            if (end != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createdAt"), end));
            }

            if (minAmount != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }

            if (maxAmount != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }

            return predicate;
        };
    }
}