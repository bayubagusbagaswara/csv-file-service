package com.bayu.csvfileservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "debit_account_product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitAccountProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productCode;

    private String fundCode;

    private String fundName;

    private String imCode;

    private String imName;

    private String currency;

    private String cashAccount;

    private String bankName;
}
