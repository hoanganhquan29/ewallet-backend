package com.wallet.ewallet.service;
import com.wallet.ewallet.entity.*;
import com.wallet.ewallet.repository.TransactionRepository;
import com.wallet.ewallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;
    public List<User> getAllUsers() {
        return userRepository.findByEnabledTrue();
    }

    public void unlockUser(String email, String ip) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        auditService.log(
                user.getId(),
                "UNLOCK_USER",
                "Admin unlocked user " + email,
                ip,
                user.getEmail()
        );
        userRepository.save(user);

    }

    public Page<Transaction> getAllTransactions(int page, int size) {
        return transactionRepository.findAll(
                PageRequest.of(page, size)
        );
    }
    public Page<Transaction> filterTransactions(
            String email,
            UUID userId,
            String type,
            String status,
            Boolean suspicious,
            Double minAmount,
            Double maxAmount,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    ) {

        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return transactionRepository.findAll((root, query, cb) -> {

            var predicate = cb.conjunction();
            if (email != null && !email.isBlank()) {
                predicate = cb.and(predicate,
                        cb.or(
                                cb.like(cb.lower(root.get("sender").get("email")), "%" + email.toLowerCase() + "%"),
                                cb.like(cb.lower(root.get("receiver").get("email")), "%" + email.toLowerCase() + "%")
                        )
                );
            }
            // filter user (sender OR receiver)
            if (userId != null) {
                predicate = cb.and(predicate,
                        cb.or(
                                cb.equal(root.get("sender").get("id"), userId),
                                cb.equal(root.get("receiver").get("id"), userId)
                        )
                );
            }

            if (type != null && !type.isBlank()) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("type"), TransactionType.valueOf(type))); // Added missing ) and ;
            }

            if (status != null && !status.isBlank()) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("status"), TransactionStatus.valueOf(status))); // Added missing ) and ;
            }

            if (suspicious != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("suspicious"), suspicious));
            }

            if (minAmount != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }

            if (maxAmount != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }

            if (from != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }

            if (to != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            return predicate;

        }, pageable);
    }
    public Page<Transaction> getSuspiciousTransactions(int page, int size) {
        return transactionRepository.findBySuspiciousTrue(
                PageRequest.of(page, size)
        );
    }
    public void lockUser(String email, String ip) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLocked(true);

        auditService.log(
                user.getId(),
                "LOCK_USER",
                "Admin locked user " + email,
                ip,
                user.getEmail()
        );

        userRepository.save(user);
    }
    public void deleteUser(String email, String ip) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(false);

        auditService.log(
                user.getId(),
                "DELETE_USER",
                "Admin deleted user " + email,
                ip,
                user.getEmail()
        );

        userRepository.save(user);
    }
    public User updateUser(String email, User updatedUser, String ip) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPhone(updatedUser.getPhone());
        user.setRole(updatedUser.getRole());

        auditService.log(
                user.getId(),
                "UPDATE_USER",
                "Admin updated user " + email,
                ip,
                user.getEmail()
        );

        return userRepository.save(user);
    }
    public User updateUserRole(String email, String role, String ip) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(Role.valueOf(role));

        return userRepository.save(user);
    }
}