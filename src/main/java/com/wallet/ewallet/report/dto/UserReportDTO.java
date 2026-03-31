package com.wallet.ewallet.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


    @Data
    @AllArgsConstructor
    public class UserReportDTO {
        private long newUsers;
        private long inactiveUsers;
        private List<Object[]> topUsers;
    }

