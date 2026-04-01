package com.wallet.ewallet.service;

import com.wallet.ewallet.dto.DepositCallbackRequest;
import com.wallet.ewallet.dto.DepositResponse;
import com.wallet.ewallet.entity.TransactionStatus;
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
    private final EmailService emailService;
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
            .status(TransactionStatus.SUCCESS)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
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
    @Transactional
    public void requestMoney(String receiverEmail, BigDecimal amount) {

        String requesterEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        if (requesterEmail.equals(receiverEmail)) {
            throw new RuntimeException("Cannot request yourself");
        }

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Transaction tx = Transaction.builder()
                .amount(amount)
                .type(TransactionType.REQUEST)
                .sender(receiver)
                .receiver(requester)
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        transactionRepository.save(tx);
    }

    @Transactional
    public void acceptRequest(UUID txId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Transaction tx = transactionRepository.findById(txId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!tx.getSender().getEmail().equals(email)) {
            throw new RuntimeException("Not your request");
        }

        if (tx.getStatus() != TransactionStatus.PENDING) {
            throw new RuntimeException("Already processed");
        }

        Wallet senderWallet = walletRepository.findByUser(tx.getSender())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        Wallet receiverWallet = walletRepository.findByUser(tx.getReceiver())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (senderWallet.getBalance().compareTo(tx.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // trừ tiền
        senderWallet.setBalance(senderWallet.getBalance().subtract(tx.getAmount()));

        // cộng tiền
        receiverWallet.setBalance(receiverWallet.getBalance().add(tx.getAmount()));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setUpdatedAt(LocalDateTime.now());

        transactionRepository.save(tx);
    }

    @Transactional
    public void rejectRequest(UUID txId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Transaction tx = transactionRepository.findById(txId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!tx.getSender().getEmail().equals(email)) {
            throw new RuntimeException("Not your request");
        }

        if (tx.getStatus() != TransactionStatus.PENDING) {
            throw new RuntimeException("Already processed");
        }

        tx.setStatus(TransactionStatus.REJECTED);
        tx.setUpdatedAt(LocalDateTime.now());

        transactionRepository.save(tx);
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

        String paymentUrl = null;

        return new DepositResponse(tx.getId().toString(), paymentUrl);
    }
    @Transactional
    public void handleDepositCallback(DepositCallbackRequest req) {
        System.out.println("CALLBACK TX ID: " + req.getTransactionId());
        UUID txId = UUID.fromString(req.getTransactionId());

        Transaction transaction = transactionRepository.findById(txId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        System.out.println("FOUND TX: " + transaction.getId());
        System.out.println("STATUS BEFORE: " + transaction.getStatus());
        // 🔥 LOG DEBUG
        System.out.println("CALLBACK TX: " + transaction.getId());
        System.out.println("STATUS BEFORE: " + transaction.getStatus());

        // ❗ CHỈ update nếu chưa SUCCESS
        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            return;
        }

        // update status
        transaction.setStatus(TransactionStatus.SUCCESS);

        // cộng tiền
        Wallet wallet = walletRepository.findByUser(transaction.getReceiver())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(transaction.getAmount()));

        walletRepository.save(wallet);
        transactionRepository.save(transaction);

        System.out.println("BALANCE UPDATED: " + wallet.getBalance());
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


        emailService.sendOtpEmail(user.getEmail(), otp);

        return "OTP sent to your email";
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
                ip,
                user.getEmail()
        );
    }
}