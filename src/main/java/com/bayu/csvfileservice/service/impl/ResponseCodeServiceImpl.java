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

    private List<String> insufficientBalanceCodes;

    @Override
    public List<String> getInsufficientBalanceCodes() {
        if (insufficientBalanceCodes == null) {
            insufficientBalanceCodes = responseCodeRepository.findCodesByName(INSUFFICIENT_BALANCE);
        }
        return insufficientBalanceCodes;
    }

    @Override
    public boolean isInsufficientBalance(String code) {
        if (code == null) return false;
        return getInsufficientBalanceCodes().contains(code);
    }

}
