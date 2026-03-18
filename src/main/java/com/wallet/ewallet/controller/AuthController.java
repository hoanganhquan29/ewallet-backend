package com.wallet.ewallet.controller;

import com.wallet.ewallet.dto.LoginRequest;
import com.wallet.ewallet.dto.RegisterRequest;
import com.wallet.ewallet.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity.ok("User registered successfully");
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {

        String token = authService.login(
                request,
                httpRequest.getRemoteAddr()
        );

        return ResponseEntity.ok(token);
    }
}