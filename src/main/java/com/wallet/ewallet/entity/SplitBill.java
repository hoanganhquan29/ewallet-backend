package com.wallet.ewallet.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "split_bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SplitBill {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private User createdBy;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private SplitBillStatus status;

    private LocalDateTime createdAt;
}