package com.wallet.ewallet.controller;

import com.wallet.ewallet.entity.Transaction;
import com.wallet.ewallet.report.dto.*;
import com.wallet.ewallet.service.UserReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/report")
@RequiredArgsConstructor
public class UserReportController {

    private final UserReportService service;

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
        return service.getByTime(
                userId,
                LocalDateTime.parse(start),
                LocalDateTime.parse(end)
        );
    }

    @GetMapping("/trend")
    public List<TrendPointDTO> trend(
            @RequestParam UUID userId,
            @RequestParam String start,
            @RequestParam String end
    ) {
        return service.getTrend(
                userId,
                LocalDateTime.parse(start),
                LocalDateTime.parse(end)
        );
    }

    @GetMapping("/top")
    public List<Transaction> top(@RequestParam UUID userId) {
        return service.getTop(userId);
    }

    @GetMapping("/latest")
    public List<Transaction> latest(@RequestParam UUID userId) {
        return service.getLatest(userId);
    }

    @GetMapping("/export")
    public String export(@RequestParam UUID userId) {
        return service.exportCSV(userId);
    }
}