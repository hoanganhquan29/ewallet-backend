package com.wallet.ewallet.repository;

import com.wallet.ewallet.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<OtpCode, UUID> {

    Optional<OtpCode> findByUserIdAndCode(UUID userId, String code);
}