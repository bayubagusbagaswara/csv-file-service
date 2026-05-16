package com.bayu.csvfileservice.model.enumerator;

import lombok.Getter;

@Getter
public enum TransactionStatus {
    READY,
    SENT,
    SUCCESS,
    FAILED,
    RETRY
}