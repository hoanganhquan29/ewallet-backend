package com.wallet.ewallet.dto;

public class DepositResponse {
    private String transactionId;
    private String paymentUrl;

    public DepositResponse(String transactionId, String paymentUrl) {
        this.transactionId = transactionId;
        this.paymentUrl = paymentUrl;
    }

    public String getTransactionId() { return transactionId; }
    public String getPaymentUrl() { return paymentUrl; }
}
