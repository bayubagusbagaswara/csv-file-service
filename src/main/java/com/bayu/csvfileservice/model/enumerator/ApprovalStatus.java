package com.bayu.csvfileservice.model.enumerator;

import lombok.Getter;

@Getter
public enum ApprovalStatus {

    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");

    private final String status;

    ApprovalStatus(String status) {
        this.status = status;
    }

}
