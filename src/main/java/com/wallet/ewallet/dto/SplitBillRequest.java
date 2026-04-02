package com.wallet.ewallet.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SplitBillRequest {

    private List<String> emails; // danh sách người
    private BigDecimal totalAmount;
    private boolean equalSplit;

    // nếu custom
    private Map<String, BigDecimal> customAmounts;
}