package com.wallet.ewallet.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditResponse {

    private String id;
    private String action;
    private String description;
    private String ip;
    private LocalDateTime time;
}