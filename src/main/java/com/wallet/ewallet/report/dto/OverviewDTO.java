package com.wallet.ewallet.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OverviewDTO {
    private long totalUsers;
    private long totalTransactions;
    private double totalAmount;
}