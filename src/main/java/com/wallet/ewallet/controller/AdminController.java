package com.wallet.ewallet.controller;

import com.wallet.ewallet.entity.AuditLog;
import com.wallet.ewallet.entity.Transaction;
import com.wallet.ewallet.entity.User;
import com.wallet.ewallet.repository.AuditLogRepository;
import com.wallet.ewallet.repository.UserRepository;
import com.wallet.ewallet.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
private final AuditLogRepository auditLogRepository;
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return adminService.getAllUsers();
    }

    @PostMapping("/unlock/{email}")
    public String unlock(
            @PathVariable String email,
            HttpServletRequest request
    ) {

        adminService.unlockUser(email, request.getRemoteAddr());

        return "User unlocked";
    }

    @GetMapping("/transactions")
    public Page<Transaction> getAllTransactions(
            @RequestParam int page,
            @RequestParam int size
    ) {
        return adminService.getAllTransactions(page, size);
    }
    @GetMapping("/audit-logs")
    public Page<AuditLog> getLogs(
            @RequestParam int page,
            @RequestParam int size
    ) {
        return auditLogRepository.findAll(PageRequest.of(page, size));
    }
    @GetMapping("/transactions/suspicious")
    public Page<Transaction> getSuspicious(
            @RequestParam int page,
            @RequestParam int size
    ) {
        return adminService.getSuspiciousTransactions(page, size);
    }
}