package com.wallet.ewallet.repository;

import com.wallet.ewallet.entity.LedgerEntry;
import com.wallet.ewallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LedgerRepository extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByWallet(Wallet wallet);

}