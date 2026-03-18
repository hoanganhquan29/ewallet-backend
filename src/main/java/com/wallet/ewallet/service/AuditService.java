package com.wallet.ewallet.service;

import com.wallet.ewallet.entity.AuditLog;
import com.wallet.ewallet.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(UUID userId, String action, String details, String ip) {

        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setDetails(details);
        log.setIpAddress(ip);
        log.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(log);
    }
}
