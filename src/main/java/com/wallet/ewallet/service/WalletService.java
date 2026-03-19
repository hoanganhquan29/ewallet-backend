package com.wallet.ewallet.service;

import com.wallet.ewallet.dto.DepositCallbackRequest;
import com.wallet.ewallet.dto.DepositResponse;
import com.wallet.ewallet.dto.TransactionStatus;
import com.wallet.ewallet.entity.OtpCode;
import com.wallet.ewallet.entity.*;
import com.wallet.ewallet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;
    private static final BigDecimal DAILY_TRANSFER_LIMIT =
            new BigDecimal("10000000");
    private final OtpRepository otpRepository;
    private final AuditService auditService;
    @Transactional
    public void transfer(String receiverEmail, BigDecimal amount) {
try {
    boolean suspicious =
            amount.compareTo(new BigDecimal("5000000")) > 0;
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new RuntimeException("Amount must be greater than 0");
    }

    String senderEmail = SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();

    if (senderEmail.equals(receiverEmail)) {
        throw new RuntimeException("Cannot transfer to yourself");
    }

    User sender = userRepository.findByEmail(senderEmail)
            .orElseThrow(() -> new RuntimeException("Sender not found"));

    User receiver = userRepository.findByEmail(receiverEmail)
            .orElseThrow(() -> new RuntimeException("Receiver not found"));

    Wallet senderWallet = walletRepository.findByUser(sender)
            .orElseThrow(() -> new RuntimeException("Sender wallet not found"));

    Wallet receiverWallet = walletRepository.findByUser(receiver)
            .orElseThrow(() -> new RuntimeException("Receiver wallet not found"));

    if (senderWallet.getBalance().compareTo(amount) < 0) {
        throw new RuntimeException("Insufficient balance");
    }
    LocalDateTime startOfDay =
            LocalDate.now().atStartOfDay();
    BigDecimal todayTotal =
            transactionRepository.getTodayTransferAmount(
                    sender.getId(),
                    startOfDay
            );

    if (todayTotal.add(amount)
            .compareTo(DAILY_TRANSFER_LIMIT) > 0) {

        throw new RuntimeException(
                "Daily transfer limit exceeded"
        );
    }
    // Trừ tiền sender
    senderWallet.setBalance(senderWallet.getBalance().subtract(amount));

    // Cộng tiền receiver
    receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

    walletRepository.save(senderWallet);
    walletRepository.save(receiverWallet);

    Transaction transaction = Transaction.builder()
            .amount(amount)
            .type(TransactionType.TRANSFER)
            .sender(sender)
            .receiver(receiver)
            .createdAt(LocalDateTime.now())
            .suspicious(suspicious)
            .build();

    transactionRepository.save(transaction);

    LedgerEntry senderEntry = LedgerEntry.builder()
            .wallet(senderWallet)
            .amount(amount.negate())
            .type(LedgerType.TRANSFER_OUT)
            .createdAt(LocalDateTime.now())
            .build();

    ledgerRepository.save(senderEntry);

    LedgerEntry receiverEntry = LedgerEntry.builder()
            .wallet(receiverWallet)
            .amount(amount)
            .type(LedgerType.TRANSFER_IN)
            .createdAt(LocalDateTime.now())
            .build();

    ledgerRepository.save(receiverEntry);
}
catch (ObjectOptimisticLockingFailureException e) {
    throw new RuntimeException("Transaction conflict, please retry");
    }
    }
    public BigDecimal getBalance() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        return wallet.getBalance();
    }

    @Transactional
    public DepositResponse requestDeposit(BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid amount");
        }

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setType(TransactionType.DEPOSIT);
        tx.setReceiver(user);
        tx.setStatus(TransactionStatus.PENDING);
        tx.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(tx);

        String paymentUrl = "http://localhost:8081/fake-gateway/pay?txId=" + tx.getId();

        return new DepositResponse(tx.getId().toString(), paymentUrl);
    }
    @Transactional
    public void handleDepositCallback(DepositCallbackRequest req) {

        Transaction tx = transactionRepository.findById(
                UUID.fromString(req.getTransactionId())
        ).orElseThrow(() -> new RuntimeException("Transaction not found"));

        // chống gọi lại
        if (tx.getStatus() != TransactionStatus.PENDING) {
            return;
        }

        // fake verify
        if (!"fake-sign".equals(req.getSignature())) {
            throw new RuntimeException("Invalid signature");
        }

        if (!"SUCCESS".equals(req.getStatus())) {
            tx.setStatus(TransactionStatus.FAILED);
            tx.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(tx);
            return;
        }

        Wallet wallet = walletRepository.findByUser(tx.getReceiver())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(tx.getAmount()));
        walletRepository.save(wallet);

        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(tx);

        LedgerEntry entry = new LedgerEntry();
        entry.setWallet(wallet);
        entry.setAmount(tx.getAmount());
        entry.setType(LedgerType.DEPOSIT);
        entry.setCreatedAt(LocalDateTime.now());

        ledgerRepository.save(entry);
    }

    public Page<Transaction> getTransactions(int page, int size) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return transactionRepository.findBySenderOrReceiver(
                user,
                user,
                PageRequest.of(page, size)
        );

    }
    public String requestTransfer(String receiverEmail, BigDecimal amount) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        // generate OTP 6 số
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        OtpCode otpCode = new OtpCode();
        otpCode.setUserId(user.getId());
        otpCode.setCode(otp);
        otpCode.setExpiredAt(LocalDateTime.now().plusMinutes(5));

        otpRepository.save(otpCode);

        System.out.println("OTP: " + otp); // demo

        return "OTP sent";
    }
    public void verifyAndTransfer(
            String receiverEmail,
            BigDecimal amount,
            String otp,
            String ip
    ) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        OtpCode otpCode = otpRepository
                .findByUserIdAndCode(user.getId(), otp)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        // check hết hạn
        if (otpCode.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        // gọi lại logic transfer CŨ của bạn
        transfer(receiverEmail, amount);

        otpRepository.delete(otpCode);
        auditService.log(
                user.getId(),
                "TRANSFER",
                "Transferred " + amount + " to " + receiverEmail,
                ip
        );
    }
}