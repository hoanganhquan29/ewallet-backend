package com.wallet.ewallet.service;

import com.wallet.ewallet.entity.Transaction;
import com.wallet.ewallet.repository.TransactionRepository;
import com.wallet.ewallet.report.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserReportService {

    private final TransactionRepository transactionRepository;

    // ===== OVERVIEW =====
    public UserStatsDTO getOverview(UUID userId) {
        List<Transaction> list = transactionRepository.findAllByUser(userId);

        double sent = 0;
        double received = 0;

        for (Transaction t : list) {

            if (t.getSender() != null && t.getSender().getId().equals(userId)) {
                sent += t.getAmount().doubleValue();
            }

            if (t.getReceiver() != null && t.getReceiver().getId().equals(userId)) {
                received += t.getAmount().doubleValue();
            }
        }

        return new UserStatsDTO(sent, received, list.size());
    }

    // ===== TIME FILTER =====
    public UserTimeStatsDTO getByTime(UUID userId, LocalDateTime start, LocalDateTime end) {
        List<Transaction> list = transactionRepository.findByUserAndTime(userId, start, end);

        double income = 0;
        double expense = 0;

        for (Transaction t : list) {

            if (t.getReceiver() != null && t.getReceiver().getId().equals(userId)) {
                income += t.getAmount().doubleValue();
            }

            if (t.getSender() != null && t.getSender().getId().equals(userId)) {
                expense += t.getAmount().doubleValue();
            }
        }

        return new UserTimeStatsDTO(income, expense);
    }

    // ===== TREND =====
    public List<TrendPointDTO> getTrend(UUID userId, LocalDateTime start, LocalDateTime end) {

        List<Transaction> list = transactionRepository.findByUserAndTime(userId, start, end);

        Map<String, Double> map = new TreeMap<>();

        for (Transaction t : list) {
            String date = t.getCreatedAt().toLocalDate().toString();

            double value = 0;

            if (t.getReceiver() != null && t.getReceiver().getId().equals(userId)) {
                value = t.getAmount().doubleValue();
            }

            if (t.getSender() != null && t.getSender().getId().equals(userId)) {
                value = -t.getAmount().doubleValue();
            }

            map.put(date, map.getOrDefault(date, 0.0) + value);
        }

        return map.entrySet().stream()
                .map(e -> new TrendPointDTO(e.getKey(), e.getValue()))
                .toList();
    }

    // ===== TOP =====
    public List<Transaction> getTop(UUID userId) {
        return transactionRepository.findTopTransactions(userId, PageRequest.of(0,5));
    }

    public List<Transaction> getLatest(UUID userId) {
        return transactionRepository.findLatestTransactions(userId, PageRequest.of(0,5));
    }

    // ===== EXPORT CSV =====
    public String exportCSV(UUID userId) {
        List<Transaction> list = transactionRepository.findAllByUser(userId);

        StringBuilder sb = new StringBuilder();
        sb.append("Date,Amount,Type\n");

        for (Transaction t : list) {
            sb.append(t.getCreatedAt())
                    .append(",")
                    .append(t.getAmount())
                    .append(",")
                    .append(t.getType())
                    .append("\n");
        }

        return sb.toString();
    }

    public List<TrendPointDTO> getMonthlyTrend(UUID userId) {
        return transactionRepository.getMonthlyTrend(userId)
                .stream()
                .map(obj -> new TrendPointDTO(
                        obj[0].toString(),
                        Double.parseDouble(obj[1].toString())
                ))
                .toList();
    }

    public List<TrendPointDTO> getYearlyTrend(UUID userId) {
        return transactionRepository.getYearlyTrend(userId)
                .stream()
                .map(obj -> new TrendPointDTO(
                        obj[0].toString(),
                        Double.parseDouble(obj[1].toString())
                ))
                .toList();
    }
}