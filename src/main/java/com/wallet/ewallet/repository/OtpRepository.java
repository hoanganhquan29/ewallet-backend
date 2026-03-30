package com.wallet.ewallet.repository;

import com.wallet.ewallet.entity.OtpCode;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<OtpCode, UUID> {

    Optional<OtpCode> findByUserIdAndCode(UUID userId, String code);
    Optional<OtpCode> findByTempToken(String tempToken);



    @Modifying
    @Transactional
    @Query("DELETE FROM OtpCode o WHERE o.userId = :userId")
    void deleteByUserId(UUID userId);
}