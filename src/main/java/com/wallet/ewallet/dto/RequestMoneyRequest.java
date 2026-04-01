package com.wallet.ewallet.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RequestMoneyRequest {
    private String receiverEmail;
    private BigDecimal amount;
}