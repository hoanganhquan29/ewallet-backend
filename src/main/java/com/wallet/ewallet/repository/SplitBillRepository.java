package com.wallet.ewallet.repository;

import com.wallet.ewallet.entity.SplitBill;
import com.wallet.ewallet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SplitBillRepository extends JpaRepository<SplitBill, UUID> {
    List<SplitBill> findByCreatedBy(User createdBy);
}