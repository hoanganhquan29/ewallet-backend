package com.wallet.ewallet.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionSummaryDTO {
    private long totalTransactions;
    private double totalAmount;
    private double depositAmount;
    private double transferAmount;
}