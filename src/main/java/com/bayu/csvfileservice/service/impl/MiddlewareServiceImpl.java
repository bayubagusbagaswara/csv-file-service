package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.apiresponse.bifastpaymentstatus.BiFastPaymentStatusRequest;
import com.bayu.csvfileservice.dto.apiresponse.bifastpaymentstatus.BiFastPaymentStatusResponse;
import com.bayu.csvfileservice.dto.apiresponse.credittransfer.CreditTransferRequest;
import com.bayu.csvfileservice.dto.apiresponse.credittransfer.CreditTransferResponse;
import com.bayu.csvfileservice.dto.apiresponse.inquiryaccount.InquiryAccountRequest;
import com.bayu.csvfileservice.dto.apiresponse.inquiryaccount.InquiryAccountResponse;
import com.bayu.csvfileservice.dto.apiresponse.overbookingcasatocasa.OverbookingCasaToCasaRequest;
import com.bayu.csvfileservice.dto.apiresponse.overbookingcasatocasa.OverbookingCasaToCasaResponse;
import com.bayu.csvfileservice.dto.apiresponse.overbookingcasatogl.OverbookingCasaToGlRequest;
import com.bayu.csvfileservice.dto.apiresponse.overbookingcasatogl.OverbookingCasaToGlResponse;
import com.bayu.csvfileservice.dto.apiresponse.sknrtgstransfer.SknRtgsTransferRequest;
import com.bayu.csvfileservice.dto.apiresponse.sknrtgstransfer.SknRtgsTransferResponse;
import com.bayu.csvfileservice.service.MiddlewareService;
import org.springframework.stereotype.Service;

@Service
public class MiddlewareServiceImpl implements MiddlewareService {

    @Override
    public OverbookingCasaToGlResponse overbookingCasaToGl(String referenceId, OverbookingCasaToGlRequest request) {
        return null;
    }

    @Override
    public OverbookingCasaToCasaResponse overbookingCasaToCasa(String referenceId, OverbookingCasaToCasaRequest request) {
        return null;
    }

    @Override
    public InquiryAccountResponse inquiryAccount(String referenceId, InquiryAccountRequest request) {
        return null;
    }

    @Override
    public CreditTransferResponse creditTransfer(String referenceId, CreditTransferRequest request) {
        return null;
    }

    @Override
    public BiFastPaymentStatusResponse paymentStatus(String referenceId, BiFastPaymentStatusRequest request) {
        return null;
    }

    @Override
    public SknRtgsTransferResponse sknRtgsTransfer(String referenceId, SknRtgsTransferRequest request) {
        return null;
    }
}
