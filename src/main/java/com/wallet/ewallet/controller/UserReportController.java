package com.wallet.ewallet.controller;

import com.wallet.ewallet.entity.Transaction;
import com.wallet.ewallet.report.dto.*;
import com.wallet.ewallet.service.UserReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/report")
@RequiredArgsConstructor
public class UserReportController {

    private final UserReportService service;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Helper map Transaction → đúng format TransactionItem cần
    private Map<String, Object> mapTransaction(Transaction t) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", t.getId());
        map.put("amount", t.getAmount());
        map.put("type", t.getType());
        map.put("status", t.getStatus());
        map.put("date", t.getCreatedAt() != null ? t.getCreatedAt().format(DATE_FORMATTER) : "");
        map.put("senderEmail", t.getSender() != null ? t.getSender().getEmail() : null);
        map.put("receiverEmail", t.getReceiver() != null ? t.getReceiver().getEmail() : null);
        return map;
    }

    @GetMapping("/overview")
    public UserStatsDTO overview(@RequestParam UUID userId) {
        return service.getOverview(userId);
    }

    @GetMapping("/time")
    public UserTimeStatsDTO byTime(
            @RequestParam UUID userId,
            @RequestParam String start,
            @RequestParam String end
    ) {
        LocalDateTime startTime = OffsetDateTime.parse(start).toLocalDateTime();
        LocalDateTime endTime = OffsetDateTime.parse(end).toLocalDateTime();
        return service.getByTime(userId, startTime, endTime);
    }

    @GetMapping("/trend")
    public List<TrendPointDTO> trend(
            @RequestParam UUID userId,
            @RequestParam String start,
            @RequestParam String end
    ) {
        LocalDateTime startTime = OffsetDateTime.parse(start).toLocalDateTime();
        LocalDateTime endTime = OffsetDateTime.parse(end).toLocalDateTime();
        return service.getTrend(userId, startTime, endTime);
    }

    @GetMapping("/top")
    public List<Map<String, Object>> top(@RequestParam UUID userId) {
        return service.getTop(userId).stream()
                .map(this::mapTransaction)
                .toList();
    }

    @GetMapping("/latest")
    public List<Map<String, Object>> latest(@RequestParam UUID userId) {
        return service.getLatest(userId).stream()
                .map(this::mapTransaction)
                .toList();
    }

    @GetMapping("/export")
    public String export(@RequestParam UUID userId) {
        return service.exportCSV(userId);
    }

    @GetMapping("/trend/monthly")
    public List<TrendPointDTO> monthly(@RequestParam UUID userId) {
        return service.getMonthlyTrend(userId);
    }

    @GetMapping("/trend/yearly")
    public List<TrendPointDTO> yearly(@RequestParam UUID userId) {
        return service.getYearlyTrend(userId);
    }
}