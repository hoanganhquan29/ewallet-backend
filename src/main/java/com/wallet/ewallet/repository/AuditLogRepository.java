package com.wallet.ewallet.repository;

import com.wallet.ewallet.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {
    Page<AuditLog> findByAction(String action, Pageable pageable);

    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);

    Page<AuditLog> findByCreatedAtBetween(
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    Page<AuditLog> findByActionAndUserId(
            String action,
            UUID userId,
            Pageable pageable
    );
}