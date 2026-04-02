package com.wallet.ewallet.repository;

import com.wallet.ewallet.entity.SplitBillDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SplitBillDetailRepository extends JpaRepository<SplitBillDetail, UUID> {

    List<SplitBillDetail> findBySplitBillId(UUID splitBillId);
}