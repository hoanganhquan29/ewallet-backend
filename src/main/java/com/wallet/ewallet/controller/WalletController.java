package com.wallet.ewallet.controller;

import com.wallet.ewallet.dto.DepositRequest;
import com.wallet.ewallet.dto.TransferRequest;
import com.wallet.ewallet.dto.VerifyTransferRequest;
import com.wallet.ewallet.entity.Transaction;
import com.wallet.ewallet.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/balance")
    public BigDecimal getBalance() {
        return walletService.getBalance();
    }
    @PostMapping("/deposit")
    public String deposit(@RequestBody DepositRequest request) {

        walletService.deposit(request.getAmount());

        return "Deposit successful";
    }
   /* @PostMapping("/transfer")
    public String transfer(@RequestBody TransferRequest request) {

        walletService.transfer(
                request.getReceiverEmail(),
                request.getAmount()
        );

        return "Transfer successful";
    }*/
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
}