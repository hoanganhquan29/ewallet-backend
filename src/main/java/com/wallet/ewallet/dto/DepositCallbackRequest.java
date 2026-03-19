package com.wallet.ewallet.dto;

import lombok.Data;

@Data
public class DepositCallbackRequest {
    private String transactionId;
    private String status;
    private String signature;
}
