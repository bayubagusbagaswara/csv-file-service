package com.bayu.csvfileservice.service;

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

public interface MiddlewareService {

    OverbookingCasaToGlResponse overbookingCasaToGl(String referenceId, OverbookingCasaToGlRequest request);

    OverbookingCasaToCasaResponse overbookingCasaToCasa(String referenceId, OverbookingCasaToCasaRequest request);

    InquiryAccountResponse inquiryAccount(String referenceId, InquiryAccountRequest request);

    CreditTransferResponse creditTransfer(String referenceId, CreditTransferRequest request);

    BiFastPaymentStatusResponse paymentStatus(String referenceId, BiFastPaymentStatusRequest request);

    SknRtgsTransferResponse sknRtgsTransfer(String referenceId, SknRtgsTransferRequest request);

}
