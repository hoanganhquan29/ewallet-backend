package com.wallet.ewallet.service;

import com.wallet.ewallet.dto.RegisterRequest;
import com.wallet.ewallet.entity.User;
import com.wallet.ewallet.entity.Role;
import com.wallet.ewallet.entity.Wallet;
import com.wallet.ewallet.repository.UserRepository;
import com.wallet.ewallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.wallet.ewallet.dto.LoginRequest;
@Service
@RequiredArgsConstructor
public class AuthService {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    public String login(LoginRequest request, String ip) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isLocked()) {
            throw new RuntimeException("Account is locked");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {

            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            if (user.getFailedLoginAttempts() >= 5) {
                user.setLocked(true);
            }

            userRepository.save(user);

            throw new RuntimeException("Invalid credentials");
        }
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        auditService.log(
                user.getId(),
                "LOGIN",
                "User logged in",
                ip
        );
        return jwtService.generateToken(user);

    }
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone already exists");
        }

        User user = User.builder()

                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .isLocked(false)
                .build();

        userRepository.save(user);
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .createdAt(LocalDateTime.now())
                .version(0L)
                .build();

        walletRepository.save(wallet);


    }
}