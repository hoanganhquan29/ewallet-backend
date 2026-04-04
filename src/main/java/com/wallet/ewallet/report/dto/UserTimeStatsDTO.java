package com.wallet.ewallet.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserTimeStatsDTO {
    private double totalIncome;
    private double totalExpense;
}