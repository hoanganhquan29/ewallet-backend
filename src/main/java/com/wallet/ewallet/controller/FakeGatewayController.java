package com.wallet.ewallet.controller;

import com.wallet.ewallet.dto.DepositCallbackRequest;
import com.wallet.ewallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fake-gateway")
@RequiredArgsConstructor
public class FakeGatewayController {

    private final WalletService walletService;

    @PostMapping("/pay")
    public String pay(@RequestParam String txId) {

        DepositCallbackRequest req = new DepositCallbackRequest();
        req.setTransactionId(txId);
        req.setStatus("SUCCESS");
        req.setSignature("fake-sign");

        walletService.handleDepositCallback(req);

        return "PAID";
    }
}