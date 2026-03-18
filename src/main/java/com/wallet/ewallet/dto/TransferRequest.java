package com.wallet.ewallet.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    private String receiverEmail;
    private BigDecimal amount;
}