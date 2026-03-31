package com.wallet.ewallet.service;

import com.wallet.ewallet.report.dto.*;
import com.wallet.ewallet.repository.TransactionRepository;
import com.wallet.ewallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

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
}