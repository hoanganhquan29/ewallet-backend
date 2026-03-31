package com.wallet.ewallet.controller;

import com.wallet.ewallet.report.dto.*;
import com.wallet.ewallet.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.wallet.ewallet.report.dto.AuditSummaryDTO;
import java.util.List;

@RestController
@RequestMapping("/api/admin/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/overview")
    public OverviewDTO overview() {
        return reportService.getOverview();
    }

    @GetMapping("/summary")
    public TransactionSummaryDTO summary() {
        return reportService.getTransactionSummary();
    }
    @GetMapping("/revenue/daily")
    public List<RevenueDTO> dailyRevenue() {
        return reportService.getDailyRevenue();
    }

    @GetMapping("/revenue/monthly")
    public List<RevenueDTO> monthlyRevenue() {
        return reportService.getMonthlyRevenue();
    }

    @GetMapping("/revenue/by-type")
    public List<RevenueDTO> byType() {
        return reportService.getAmountByType();
    }
    @GetMapping("/users")
    public UserReportDTO userReport() {
        return reportService.getUserReport();
    }
    @GetMapping("/audit-summary")
    public AuditSummaryDTO auditSummary() {
        return reportService.getAuditSummary();
    }
}