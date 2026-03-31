package com.wallet.ewallet.controller;

import com.wallet.ewallet.dto.UpdateUserRequest;
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
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import com.wallet.ewallet.dto.AuditResponse;
import java.util.List;
import java.util.Map;

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
    @PostMapping("/lock/{email}")
    public String lock(
            @PathVariable String email,
            HttpServletRequest request
    ) {
        adminService.lockUser(email, request.getRemoteAddr());
        return "User locked";
    }
    @DeleteMapping("/users/{email}")
    public String deleteUser(
            @PathVariable String email,
            HttpServletRequest request
    ) {
        adminService.deleteUser(email, request.getRemoteAddr());
        return "User deleted";
    }
    /*@PutMapping("/users/{email}")
    public User updateUser(
            @PathVariable String email,
            @RequestBody User user,
            HttpServletRequest request
    ) {
        return adminService.updateUser(email, user, request.getRemoteAddr());
    }*/
    @PutMapping("/users/{email}")
    public User updateUser(
            @PathVariable String email,
            @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest
    ) {
        return adminService.updateUserRole(
                email,
                request.getRole(),
                httpRequest.getRemoteAddr()
        );
    }
    @GetMapping("/transactions")
    public Page<Transaction> getAllTransactions(
            @RequestParam int page,
            @RequestParam int size
    ) {
        return adminService.getAllTransactions(page, size);
    }
    @GetMapping("/audit-logs")
    public Page<AuditResponse> getLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        UUID userUUID = null;

        try {
            if (userId != null && !userId.isBlank()) {
                userUUID = UUID.fromString(userId);
            }
        } catch (IllegalArgumentException e) {

            userUUID = null;
        }
        LocalDateTime fromDate = (from != null) ? LocalDateTime.parse(from) : null;
        LocalDateTime toDate = (to != null) ? LocalDateTime.parse(to) : null;

        Page<AuditLog> logs;

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        final String finalAction = action;
        final UUID finalUserUUID = userUUID;
        final LocalDateTime finalFromDate = fromDate;
        final LocalDateTime finalToDate = toDate;
        logs = auditLogRepository.findAll((root, query, cb) -> {

            var predicate = cb.conjunction();

            if (finalAction != null && !finalAction.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("action"), finalAction));
            }

            if (finalUserUUID != null) {
                predicate = cb.and(predicate, cb.equal(root.get("userId"), finalUserUUID));
            }

            if (finalFromDate != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("createdAt"), finalFromDate));
            }

            if (finalToDate != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("createdAt"), finalToDate));
            }
            return predicate;

        }, pageable);

        return logs.map(this::mapToResponse);
    }
    @GetMapping("/transactions/suspicious")
    public Page<Transaction> getSuspicious(
            @RequestParam int page,
            @RequestParam int size
    ) {
        return adminService.getSuspiciousTransactions(page, size);
    }
    private AuditResponse mapToResponse(AuditLog log) {
        return AuditResponse.builder()
                .id(log.getId().toString())
                .action(log.getAction())
                .description(parseDetails(log))
                .ip(log.getIpAddress())
                .time(log.getCreatedAt())
                .build();
    }
    private String parseDetails(AuditLog log) {

        String details = log.getDetails();

        if (details == null) return log.getAction();

        try {
            int start = details.indexOf("email") + 8;
            int end = details.indexOf("\"", start);
            String email = details.substring(start, end);

            return switch (log.getAction()) {
                case "LOGIN" -> "User " + email + " logged in";
                case "LOCK_USER" -> "Admin locked " + email;
                case "UNLOCK_USER" -> "Admin unlocked " + email;
                case "DELETE_USER" -> "Admin deleted " + email;
                case "UPDATE_USER" -> "Admin updated " + email;
                default -> log.getAction() + " - " + email;
            };

        } catch (Exception e) {
            return log.getAction();
        }
    }
}