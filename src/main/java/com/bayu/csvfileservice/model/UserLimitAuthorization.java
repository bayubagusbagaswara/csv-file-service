package com.bayu.csvfileservice.model;

import com.bayu.csvfileservice.model.base.BaseApproval;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_limit_authorizations")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserLimitAuthorization extends BaseApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "username")
    private String username;

    @Column(name = "user_group")
    private String userGroup;

    @Column(name = "limit_amount")
    private BigDecimal limit;

}
