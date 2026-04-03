package com.bayu.csvfileservice.model.base;

import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
public abstract class BaseApproval {

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    private String inputId;

    private LocalDateTime inputDate;

    private String inputIpAddress;

    private String approveId;

    private LocalDateTime approveDate;

    private String approveIpAddress;

}
