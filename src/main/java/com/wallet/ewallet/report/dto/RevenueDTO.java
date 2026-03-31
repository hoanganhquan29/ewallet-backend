package com.wallet.ewallet.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RevenueDTO {
    private String date;
    private double amount;
}