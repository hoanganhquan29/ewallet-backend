package com.wallet.ewallet.controller;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.wallet.ewallet.dto.*;
import com.wallet.ewallet.entity.Transaction;
import com.wallet.ewallet.entity.TransactionType;
import com.wallet.ewallet.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.wallet.ewallet.dto.RequestMoneyRequest;

import java.math.BigDecimal;
import com.wallet.ewallet.dto.SplitBillResponse;

import java.util.Map;
import com.wallet.ewallet.dto.SplitBillRequest;
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/balance")
    public BigDecimal getBalance() {
        return walletService.getBalance();
    }

    @GetMapping("/transactions")
    public Page<?> transactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,

            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount
    ) {

        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;

        var result = walletService.filterTransactions(
                page, size, type, start, end, minAmount, maxAmount
        );


        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return result.map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("amount", t.getAmount());
            map.put("type", t.getType());
            map.put("date", t.getCreatedAt().format(f));
            map.put("senderEmail", t.getSender() != null ? t.getSender().getEmail() : null);
            map.put("receiverEmail", t.getReceiver() != null ? t.getReceiver().getEmail() : null);
            map.put("status", t.getStatus());
            return map;
        });
    }

    @PostMapping("/transfer/request")
    public String requestTransfer(@RequestBody TransferRequest request) {

        return walletService.requestTransfer(
                request.getReceiverEmail(),
                request.getAmount()
        );
    }
    @PostMapping("/transfer/verify")
    public String verifyTransfer(
            @RequestBody VerifyTransferRequest request,
            HttpServletRequest httpRequest
    ) {

        walletService.verifyAndTransfer(
                request.getReceiverEmail(),
                request.getAmount(),
                request.getOtp(),
                httpRequest.getRemoteAddr()
        );

        return "Transfer successful";
    }
    @PostMapping("/deposit/request")
    public Map<String, String> requestDeposit(@RequestBody DepositRequest req) throws Exception {

        // 1. tạo transaction PENDING
        DepositResponse res = walletService.requestDeposit(req.getAmount());

        String transactionId = res.getTransactionId();

        // 2. tạo Stripe session
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(req.getSuccessUrl())
                        .setCancelUrl(req.getCancelUrl())
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("vnd") //
                                                        .setUnitAmount(req.getAmount().longValue()) 
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Wallet Deposit")
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .setClientReferenceId(transactionId)
                        .putMetadata("transactionId", transactionId)
                        .build();
        System.out.println("TransactionId: " + transactionId);
        Session session = Session.create(params);

        return Map.of("url", session.getUrl());
    }

    @PostMapping("/deposit/callback")
    public void callback(@RequestBody DepositCallbackRequest req) {
        walletService.handleDepositCallback(req);
    }

    @PostMapping("/request-money")
    public String requestMoney(@RequestBody RequestMoneyRequest req) {
        walletService.requestMoney(
                req.getReceiverEmail(),
                req.getAmount()
        );
        return "Request sent";
    }

    @PostMapping("/request-money/{id}/accept")
    public String acceptRequest(@PathVariable UUID id) {
        walletService.acceptRequest(id);
        return "Accepted";
    }

    @PostMapping("/request-money/{id}/reject")
    public String rejectRequest(@PathVariable UUID id) {
        walletService.rejectRequest(id);
        return "Rejected";
    }

    @GetMapping("/request-money/pending")
    public List<Transaction> getPendingRequests() {
        return walletService.getPendingRequests();
    }

    @PostMapping("/split-bill")
    public String splitBill(@RequestBody SplitBillRequest req) {

        walletService.splitBill(
                req.getEmails(),
                req.getTotalAmount(),
                req.isEqualSplit(),
                req.getCustomAmounts()
        );

        return "Split bill sent";
    }

    @PostMapping("/split-bill/{detailId}/accept")
    public String acceptSplit(@PathVariable UUID detailId) {
        walletService.acceptSplit(detailId);
        return "Paid";
    }

    @PostMapping("/split-bill/{detailId}/reject")
    public String rejectSplit(@PathVariable UUID detailId) {
        walletService.rejectSplit(detailId);
        return "Rejected";
    }

    @GetMapping("/split-bill")
    public List<SplitBillResponse> getMySplitBills() {
        return walletService.getMySplitBills();
    }

    @GetMapping("/split-bill/{id}")
    public SplitBillResponse getSplitBillDetail(@PathVariable UUID id) {
        return walletService.getSplitBillDetail(id);
    }
}