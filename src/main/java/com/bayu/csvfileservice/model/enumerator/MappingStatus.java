package com.bayu.csvfileservice.model.enumerator;

import lombok.Getter;

@Getter
public enum MappingStatus {

    DRAFT,
    READY,
    SENT,
    SUCCESS,
    FAILED,
    RETRY

}
