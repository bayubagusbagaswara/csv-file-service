package com.bayu.csvfileservice.service;

import java.util.List;

public interface ResponseCodeService {

    List<String> getInsufficientBalanceCodes();

    boolean isInsufficientBalance(String code);

}
