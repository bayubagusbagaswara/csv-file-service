package com.bayu.csvfileservice.model;

import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_limit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TransactionLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TransferMethod transferMethod;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

}
