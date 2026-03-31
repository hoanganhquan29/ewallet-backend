package com.wallet.ewallet.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.LocalDateTime;
import jakarta.persistence.Column;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean isLocked = false;

    @Column(nullable = false)
    private Integer failedLoginAttempts = 0;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime lastLogin;
}