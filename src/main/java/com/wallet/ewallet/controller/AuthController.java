package com.wallet.ewallet.controller;

import com.wallet.ewallet.dto.GoogleLoginRequest;
import com.wallet.ewallet.dto.LoginRequest;
import com.wallet.ewallet.dto.RegisterRequest;
import com.wallet.ewallet.service.AuthService;
import com.wallet.ewallet.service.GoogleTokenVerifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleTokenVerifier googleTokenVerifier;

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    // LOGIN EMAIL
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        try {
            String result = authService.login(request, httpRequest.getRemoteAddr());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body,
                                       HttpServletRequest request) {
        try {
            String token = authService.verifyOtp(
                    body.get("email"),
                    body.get("otp"),
                    request.getRemoteAddr()
            );
            return ResponseEntity.ok(token);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody GoogleLoginRequest request) {

        var payload = googleTokenVerifier.verify(request.getIdToken());

        String email = payload.getEmail();


        String token = authService.loginWithGoogle(email);

        return ResponseEntity.ok(token);
    }
}