package com.wallet.ewallet.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuditSummaryDTO {
    private long loginFail;
    private long lockUser;
    private long adminActions;
}