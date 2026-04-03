package com.bayu.csvfileservice.model;

import com.bayu.csvfileservice.model.base.BaseApproval;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "master_bank",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_master_bank_bank_code", columnNames = "bank_code"),
                @UniqueConstraint(name = "uk_master_bank_bi_code", columnNames = "bi_code")
        }
)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MasterBank extends BaseApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_code", nullable = false, length = 20)
    private String bankCode;

    private String bankName;

    @Column(name = "bi_code", nullable = false, length = 20)
    private String biCode;

    private String bankType;

    private String branchCode;

}
