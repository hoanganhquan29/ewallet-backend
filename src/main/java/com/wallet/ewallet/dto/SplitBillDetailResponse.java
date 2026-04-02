package com.wallet.ewallet.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SplitBillDetailResponse {

    private UUID id;
    private String email;
    private BigDecimal amount;
    private String status;
}