package com.wallet.ewallet.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VerifyTransferRequest {

    private String receiverEmail;

    private BigDecimal amount;

    private String otp;
}