package com.bayu.csvfileservice.model;

import com.bayu.csvfileservice.model.base.BaseApproval;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "debit_account_product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitAccountProduct extends BaseApproval {

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
