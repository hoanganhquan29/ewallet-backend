package com.wallet.ewallet.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
@Entity
@Data
public class OtpCode {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID userId;

    private String code;

    private LocalDateTime expiredAt;
    @Column(unique = true)
    private String tempToken;

}