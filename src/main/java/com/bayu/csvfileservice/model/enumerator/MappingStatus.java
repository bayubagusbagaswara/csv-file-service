package com.bayu.csvfileservice.model.enumerator;

import lombok.Getter;

@Getter
public enum MappingStatus {

    DRAFT,
    HOLD,
    READY,
    SENT,
    SUCCESS,
    FAILED,
    RETRY
}
