package com.wallet.ewallet.service;
import org.springframework.transaction.annotation.Transactional;
import com.wallet.ewallet.dto.RegisterRequest;
import com.wallet.ewallet.entity.OtpCode;
import com.wallet.ewallet.entity.User;
import com.wallet.ewallet.entity.Role;
import com.wallet.ewallet.entity.Wallet;
import com.wallet.ewallet.repository.UserRepository;
import com.wallet.ewallet.repository.WalletRepository;
import com.wallet.ewallet.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.wallet.ewallet.dto.LoginRequest;
import org.springframework.security.core.context.SecurityContextHolder;
@Service
@RequiredArgsConstructor
public class AuthService {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final OtpRepository otpRepository;
    private final EmailService emailService;
    @Transactional(noRollbackFor = RuntimeException.class)
    public String login(LoginRequest request, String ip) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email does not exist"));

        if (user.isLocked()) {
            auditService.log(user.getId(), "LOGIN_BLOCKED", "Account locked", ip, user.getEmail());
            throw new RuntimeException("Account is locked. Please contact admin");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {

            int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= 5) {
                user.setLocked(true);

                userRepository.saveAndFlush(user);

                auditService.log(user.getId(), "ACCOUNT_LOCKED",
                        "Locked after 5 failed attempts", ip, user.getEmail());

                throw new RuntimeException("Account locked after 5 failed attempts");
            }

            userRepository.saveAndFlush(user);

            auditService.log(user.getId(), "LOGIN_FAILED",
                    "Wrong password", ip, user.getEmail());

            throw new RuntimeException("Wrong password (" + attempts + "/5)");
        }

        user.setFailedLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        auditService.log(user.getId(), "LOGIN_PASSWORD_OK",
                "Password correct, require OTP", ip, user.getEmail());

        // ===== 2FA =====
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

// xóa OTP cũ
        otpRepository.deleteByUserId(user.getId());

        OtpCode otpCode = new OtpCode();
        otpCode.setUserId(user.getId());
        otpCode.setCode(otp);
        otpCode.setExpiredAt(LocalDateTime.now().plusMinutes(5));

        otpRepository.save(otpCode);

// gửi email (DÙNG LẠI CODE CỦA BẠN)
        emailService.sendOtpEmail(user.getEmail(), otp);

        auditService.log(user.getId(), "OTP_SENT",
                "OTP sent for login", ip, user.getEmail());

        return "OTP sent to email";
    }
    public String verifyOtp(String email, String otpInput, String ip) {
        System.out.println("========== VERIFY OTP ==========");
        System.out.println("INPUT EMAIL: " + email);
        System.out.println("INPUT OTP: " + otpInput);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("USER ID: " + user.getId());

// lấy tất cả OTP của user để debug
        var list = otpRepository.findAll();
        for (OtpCode o : list) {
            System.out.println("DB OTP -> userId: " + o.getUserId()
                    + " | code: " + o.getCode()
                    + " | expired: " + o.getExpiredAt());
        }
        OtpCode otpCode = otpRepository
                .findByUserIdAndCode(user.getId(), otpInput)
                .orElse(null);

        if (otpCode == null) {
            throw new RuntimeException("Wrong OTP");
        }
        System.out.println("✅ OTP MATCH FOUND");

        if (otpCode.getExpiredAt().isBefore(LocalDateTime.now())) {
            System.out.println("❌ OTP EXPIRED");
            throw new RuntimeException("OTP expired");
        }
        if (otpCode.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        String token = jwtService.generateToken(user);

        otpRepository.delete(otpCode);

        auditService.log(user.getId(), "LOGIN_SUCCESS_2FA",
                "Login success", ip, user.getEmail());

        return token;
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

        user.setCreatedAt(LocalDateTime.now());
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
    public String loginWithGoogle(String email) {

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .role(Role.USER)
                            .isLocked(false)
                            .build();

                    userRepository.save(newUser);

                    // 🔥 TẠO WALLET LUÔN (QUAN TRỌNG)
                    Wallet wallet = Wallet.builder()
                            .user(newUser)
                            .balance(BigDecimal.ZERO)
                            .currency("VND")
                            .createdAt(LocalDateTime.now())
                            .version(0L)
                            .build();

                    walletRepository.save(wallet);

                    return newUser;
                });

        // log giống login thường
        auditService.log(
                user.getId(),
                "LOGIN_GOOGLE",
                "User logged in with Google",
                "GOOGLE",
                user.getEmail()
        );

        return jwtService.generateToken(user);
    }
}