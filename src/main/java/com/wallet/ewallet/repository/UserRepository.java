package com.wallet.ewallet.repository;

import com.wallet.ewallet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<User> findByEnabledTrue();
    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();
    @Query("""
    SELECT COUNT(u)
    FROM User u
    WHERE u.createdAt >= :date
""")
    long countNewUsers(LocalDateTime date);

    @Query("""
    SELECT COUNT(u)
    FROM User u
    WHERE u.lastLogin < :date OR u.lastLogin IS NULL
""")
    long countInactiveUsers(LocalDateTime date);
}