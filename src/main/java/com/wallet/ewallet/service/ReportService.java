package com.wallet.ewallet.service;

import com.wallet.ewallet.report.dto.*;
import com.wallet.ewallet.repository.TransactionRepository;
import com.wallet.ewallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.wallet.ewallet.report.dto.AuditSummaryDTO;
import com.wallet.ewallet.repository.AuditLogRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    public OverviewDTO getOverview() {
        long totalUsers = userRepository.countAllUsers();
        long totalTransactions = transactionRepository.countAllTransactions();
        double totalAmount = transactionRepository.sumAllAmount();

        return new OverviewDTO(totalUsers, totalTransactions, totalAmount);
    }

    public TransactionSummaryDTO getTransactionSummary() {
        long totalTransactions = transactionRepository.countAllTransactions();
        double totalAmount = transactionRepository.sumAllAmount();
        double deposit = transactionRepository.sumDeposit();
        double transfer = transactionRepository.sumTransfer();

        return new TransactionSummaryDTO(
                totalTransactions,
                totalAmount,
                deposit,
                transfer
        );
    }
    public List<RevenueDTO> getDailyRevenue() {
        return transactionRepository.getDailyRevenue()
                .stream()
                .map(obj -> new RevenueDTO(
                        obj[0].toString(),
                        Double.parseDouble(obj[1].toString())
                ))
                .toList();
    }

    public List<RevenueDTO> getMonthlyRevenue() {
        return transactionRepository.getMonthlyRevenue()
                .stream()
                .map(obj -> new RevenueDTO(
                        obj[0].toString(),
                        Double.parseDouble(obj[1].toString())
                ))
                .toList();
    }

    public List<RevenueDTO> getAmountByType() {
        return transactionRepository.getAmountByType()
                .stream()
                .map(obj -> new RevenueDTO(
                        obj[0].toString(),
                        Double.parseDouble(obj[1].toString())
                ))
                .toList();
    }
    public UserReportDTO getUserReport() {

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        long newUsers = userRepository.countNewUsers(sevenDaysAgo);
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long inactiveUsers = userRepository.countInactiveUsers(thirtyDaysAgo);

        List<Object[]> topUsers = transactionRepository.getTopUsers(
                PageRequest.of(0, 5)
        );

        return new UserReportDTO(newUsers, inactiveUsers, topUsers);
    }
    public AuditSummaryDTO getAuditSummary() {
        return new AuditSummaryDTO(
                auditLogRepository.countLoginFail(),
                auditLogRepository.countLockUser(),
                auditLogRepository.countAdminActions()
        );
    }
}