package com.wallet.ewallet.repository;

import com.wallet.ewallet.entity.SplitBill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SplitBillRepository extends JpaRepository<SplitBill, UUID> {
}