package com.wallet.ewallet.controller;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.wallet.ewallet.dto.*;
import com.wallet.ewallet.entity.Transaction;
import com.wallet.ewallet.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    public Page<Transaction> transactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return walletService.getTransactions(page, size);
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
                        .setCancelUrl(req.getSuccessUrl())
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("usd")
                                                        .setUnitAmount((long) (req.getAmount().doubleValue() * 100))
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
}