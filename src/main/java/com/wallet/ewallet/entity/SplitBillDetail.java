package com.wallet.ewallet.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "split_bill_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SplitBillDetail {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private SplitBill splitBill;

    @ManyToOne
    private User user;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
}