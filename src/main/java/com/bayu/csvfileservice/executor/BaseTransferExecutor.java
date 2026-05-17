package com.bayu.csvfileservice.executor;

import com.bayu.csvfileservice.dto.CreateNcbsRequest;
import com.bayu.csvfileservice.dto.CreateNcbsResponse;
import com.bayu.csvfileservice.model.NcbsRequest;
import com.bayu.csvfileservice.model.NcbsResponse;
import com.bayu.csvfileservice.model.enumerator.MiddlewareServiceType;
import com.bayu.csvfileservice.service.NcbsRequestService;
import com.bayu.csvfileservice.service.NcbsResponseService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public abstract class BaseTransferExecutor implements TransferExecutor {

    protected final ObjectMapper objectMapper;
    protected final NcbsRequestService ncbsRequestService;
    protected final NcbsResponseService ncbsResponseService;

    protected BaseTransferExecutor(
            ObjectMapper objectMapper,
            NcbsRequestService ncbsRequestService,
            NcbsResponseService ncbsResponseService
    ) {
        this.objectMapper = objectMapper;
        this.ncbsRequestService = ncbsRequestService;
        this.ncbsResponseService = ncbsResponseService;
    }

    protected NcbsRequest createNcbsRequest(
            Transferable item,
            String referenceId,
            MiddlewareServiceType serviceType,
            Object requestBody
    ) {
        try {
            String jsonRequest = objectMapper.writeValueAsString(requestBody);

//            NcbsRequest request = NcbsRequest.builder()
//                    .referenceId(referenceId)
//                    .entityId(item.getId())
//                    .featureType(item.getFeatureType())
//                    .service(serviceType)
//                    .transferScope(item.getTransferScope())
//                    .transferMethod(item.getTransferMethod())
//                    .processType(item.getProcessType())
//                    .jsonRequest(jsonRequest)
//                    .createdDate(LocalDateTime.now())
//                    .build();

            CreateNcbsRequest request = new CreateNcbsRequest();

            return ncbsRequestService.create(request);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to create NCBS request for referenceId: " + referenceId,
                    e
            );
        }
    }

    protected NcbsResponse createNcbsResponse(
            Transferable item,
            String referenceId,
            MiddlewareServiceType serviceType,
            Object responseBody
    ) {
        try {
            String jsonResponse = objectMapper.writeValueAsString(responseBody);

//            NcbsResponse response = NcbsResponse.builder()
//                    .referenceId(referenceId)
//                    .entityId(item.getId())
//                    .featureType(item.getFeatureType())
//                    .service(serviceType)
//                    .transferScope(item.getTransferScope())
//                    .transferMethod(item.getTransferMethod())
//                    .processType(item.getProcessType())
//                    .responseCode(extractResponseCode(responseBody))
//                    .responseMessage(extractResponseMessage(responseBody))
//                    .jsonResponse(jsonResponse)
//                    .createdDate(LocalDateTime.now())
//                    .build();

            CreateNcbsResponse response = new CreateNcbsResponse();

            return ncbsResponseService.create(response);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to create NCBS response for referenceId: " + referenceId,
                    e
            );
        }
    }

    protected String generateReferenceId() {
        return UUID.randomUUID().toString();
    }

    protected TransferExecutionResult buildExecutionResult(
            String inquiryReferenceId,
            String referenceId,
            NcbsResponse finalResponse
    ) {
        if (finalResponse == null) {
            return TransferExecutionResult.builder()
                    .inquiryReferenceId(inquiryReferenceId)
                    .referenceId(referenceId)
                    .responseCode(null)
                    .responseMessage(null)
                    .finalResponse(null)
                    .build();
        }

        return TransferExecutionResult.builder()
                .inquiryReferenceId(inquiryReferenceId)
                .referenceId(referenceId)
                .responseCode(finalResponse.getResponseCode())
                .responseMessage(finalResponse.getResponseMessage())
                .finalResponse(finalResponse)
                .build();
    }

    protected String extractResponseCode(Object responseBody) {
        if (responseBody == null) {
            return null;
        }

        try {
            JsonNode node = objectMapper.valueToTree(responseBody);

            if (node.has("responseCode")) {
                return node.get("responseCode").asText();
            }

            if (node.has("code")) {
                return node.get("code").asText();
            }

            if (node.has("statusCode")) {
                return node.get("statusCode").asText();
            }

            return null;

        } catch (Exception e) {
            log.warn("Failed to extract responseCode from responseBody", e);
            return null;
        }
    }

    protected String extractResponseMessage(Object responseBody) {
        if (responseBody == null) {
            return null;
        }

        try {
            JsonNode node = objectMapper.valueToTree(responseBody);

            if (node.has("responseMessage")) {
                return node.get("responseMessage").asText();
            }

            if (node.has("message")) {
                return node.get("message").asText();
            }

            if (node.has("statusMessage")) {
                return node.get("statusMessage").asText();
            }

            return null;

        } catch (Exception e) {
            log.warn("Failed to extract responseMessage from responseBody", e);
            return null;
        }
    }

}
