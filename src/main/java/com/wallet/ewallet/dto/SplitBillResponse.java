package com.wallet.ewallet.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class SplitBillResponse {

    private UUID id;
    private BigDecimal totalAmount;
    private String status;
    private String createdByEmail;
    private List<SplitBillDetailResponse> details;
}