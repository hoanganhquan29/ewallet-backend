package com.wallet.ewallet.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionResponse {
    public UUID id;
    public BigDecimal amount;
    public String type;
    public String date;
}