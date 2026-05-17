package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.dto.apiresponse.overbookingcasatocasa.OverbookingCasaToCasaRequest;
import com.bayu.csvfileservice.dto.apiresponse.overbookingcasatocasa.OverbookingCasaToCasaResponse;
import com.bayu.csvfileservice.dto.apiresponse.overbookingcasatogl.OverbookingCasaToGlRequest;
import com.bayu.csvfileservice.dto.apiresponse.overbookingcasatogl.OverbookingCasaToGlResponse;
import com.bayu.csvfileservice.executor.BaseTransferExecutor;
import com.bayu.csvfileservice.executor.TransferExecutionResult;
import com.bayu.csvfileservice.executor.Transferable;
import com.bayu.csvfileservice.model.NcbsResponse;
import com.bayu.csvfileservice.model.enumerator.MiddlewareServiceType;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import com.bayu.csvfileservice.util.MiddlewareServiceTypeResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OverbookingExecutor extends BaseTransferExecutor {

    private final MiddlewareServiceTypeResolver serviceTypeResolver;
    private final MiddlewareService middlewareService;

    public OverbookingExecutor(
            ObjectMapper objectMapper,
            NcbsRequestService ncbsRequestService,
            NcbsResponseService ncbsResponseService,
            MiddlewareServiceTypeResolver serviceTypeResolver,
            MiddlewareService middlewareService
    ) {
        super(objectMapper, ncbsRequestService, ncbsResponseService);
        this.serviceTypeResolver = serviceTypeResolver;
        this.middlewareService = middlewareService;
    }

    @Override
    public boolean supports(TransferMethod transferMethod) {
        return TransferMethod.OVERBOOKING.equals(transferMethod);
    }

    @Override
    public TransferExecutionResult execute(Transferable item) {

        MiddlewareServiceType serviceType =
                serviceTypeResolver.resolve(
                        item.getFeatureType(),
                        item.getTransferMethod()
                );

        if (MiddlewareServiceType.OVERBOOKING_CASA_TO_CASA.equals(serviceType)) {
            return processCasaToCasa(item);
        }

        if (MiddlewareServiceType.OVERBOOKING_CASA_TO_GL.equals(serviceType)) {
            return processCasaToGl(item);
        }

        throw new IllegalArgumentException(
                "Unsupported overbooking serviceType: " + serviceType
        );
    }

    private TransferExecutionResult processCasaToCasa(Transferable item) {

        String referenceId = generateReferenceId();

        OverbookingCasaToCasaRequest request = buildCasaToCasaRequest(item);

        createNcbsRequest(
                item,
                referenceId,
                MiddlewareServiceType.OVERBOOKING_CASA_TO_CASA,
                request
        );

        OverbookingCasaToCasaResponse response = middlewareService.overbookingCasaToCasa(referenceId, request);

        NcbsResponse ncbsResponse = createNcbsResponse(
                item,
                referenceId,
                MiddlewareServiceType.OVERBOOKING_CASA_TO_CASA,
                response
        );

        return buildExecutionResult(null, referenceId, ncbsResponse);
    }

    private TransferExecutionResult processCasaToGl(Transferable item) {

        String referenceId = generateReferenceId();

        OverbookingCasaToGlRequest request = buildCasaToGlRequest(item);

        createNcbsRequest(
                item,
                referenceId,
                MiddlewareServiceType.OVERBOOKING_CASA_TO_GL,
                request
        );

        OverbookingCasaToGlResponse response = middlewareService.overbookingCasaToGl(referenceId, request);

        NcbsResponse ncbsResponse = createNcbsResponse(
                item,
                referenceId,
                MiddlewareServiceType.OVERBOOKING_CASA_TO_GL,
                response
        );

        return buildExecutionResult(null, referenceId, ncbsResponse);
    }

    private OverbookingCasaToCasaRequest buildCasaToCasaRequest(Transferable item) {
        return OverbookingCasaToCasaRequest.builder()
                //.debitAccount(item.getDebitAccount())
//                .creditAccount(item.getCreditAccount())
//                .amount(item.getAmount())
//                .description(item.getDescription())
                .build();
    }

    private OverbookingCasaToGlRequest buildCasaToGlRequest(Transferable item) {
        return OverbookingCasaToGlRequest.builder()
//                .debitAccount(item.getDebitAccount())
//                .glAccount(item.getCreditAccount())
//                .amount(item.getAmount())
//                .description(item.getDescription())
                .build();
    }
}