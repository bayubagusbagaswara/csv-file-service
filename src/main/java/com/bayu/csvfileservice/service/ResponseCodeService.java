package com.bayu.csvfileservice.service;

import java.util.List;

public interface ResponseCodeService {

    List<String> getInsufficientBalanceCodes();

    List<String> getSuccessCodes();

    boolean isInsufficientBalance(String code);

    boolean isSuccess(String code);

}
