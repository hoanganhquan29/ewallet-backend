package com.wallet.ewallet.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import lombok.Data;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class AuditLog {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID userId;

    private String action;

    private String details;

    private String ipAddress;

    private LocalDateTime createdAt;
}
