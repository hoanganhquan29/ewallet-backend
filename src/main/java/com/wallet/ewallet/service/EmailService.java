package com.wallet.ewallet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("E-Wallet OTP Verification");
        message.setText(
                "Your OTP code is: " + otp +
                        "\n\nThis code will expire in 5 minutes."
        );

        mailSender.send(message);
    }
}