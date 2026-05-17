package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.repository.ResponseCodeRepository;
import com.bayu.csvfileservice.service.ResponseCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResponseCodeServiceImpl implements ResponseCodeService {

    private final ResponseCodeRepository responseCodeRepository;

    public static final String INSUFFICIENT_BALANCE = "INSUFFICIENT_BALANCE";
    public static final String SUCCESS = "SUCCESS";

    private List<String> insufficientBalanceCodes;
    private List<String> successCodes;

    @Override
    public List<String> getInsufficientBalanceCodes() {
        if (insufficientBalanceCodes == null) {
            insufficientBalanceCodes = responseCodeRepository.findCodesByName(INSUFFICIENT_BALANCE);
        }
        return insufficientBalanceCodes;
    }

    @Override
    public List<String> getSuccessCodes() {
        if (successCodes == null) {
            successCodes = responseCodeRepository.findCodesByName(SUCCESS);
        }
        return successCodes;
    }

    @Override
    public boolean isInsufficientBalance(String code) {
        if (code == null) return false;
        return getInsufficientBalanceCodes().contains(code);
    }

    @Override
    public boolean isSuccess(String code) {
        if (code == null) return false;
        return getSuccessCodes().contains(code);
    }

}
