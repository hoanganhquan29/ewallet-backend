package com.wallet.ewallet.controller;

import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.wallet.ewallet.dto.DepositCallbackRequest;
import com.wallet.ewallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final WalletService walletService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            System.out.println("EVENT TYPE: " + event.getType());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        if ("checkout.session.completed".equals(event.getType())) {

            String json = event.getDataObjectDeserializer().getRawJson();

            Session session = Session.GSON.fromJson(json, Session.class);

            System.out.println("CLIENT REF: " + session.getClientReferenceId());
            System.out.println("METADATA TX: " + session.getMetadata().get("transactionId"));
            String transactionId = session.getMetadata().get("transactionId");

            System.out.println("WEBHOOK TX ID: " + transactionId);

            DepositCallbackRequest req = new DepositCallbackRequest();
            req.setTransactionId(transactionId);
            req.setStatus("SUCCESS");
            req.setSignature("fake-sign");

            walletService.handleDepositCallback(req);
        }

        return ResponseEntity.ok("ok");
    }

}