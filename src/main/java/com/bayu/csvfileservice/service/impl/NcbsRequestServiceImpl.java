package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.CreateNcbsRequest;
import com.bayu.csvfileservice.model.NcbsRequest;
import com.bayu.csvfileservice.model.enumerator.FeatureType;
import com.bayu.csvfileservice.repository.NcbsRequestRepository;
import com.bayu.csvfileservice.service.NcbsRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NcbsRequestServiceImpl implements NcbsRequestService {

    private static final String NCBS_REQUEST_STR = "Ncbs Request";

    private final NcbsRequestRepository ncbsRequestRepository;

    @Override
    public NcbsRequest create(CreateNcbsRequest request) {
        if (isReferenceIdAlreadyExists(request.getReferenceId())) {
            log.info("Transaction for ReferenceId {} has been approved (send transaction).", request.getReferenceId());

            if (request.getFeatureType().equals(FeatureType.PLACEMENT_DEPOSIT_TRANSFER)) {
                throw new IllegalStateException(
                        "Transaction for DepositTransfer with SiReferenceId " + request.getSiReferenceId() + " has been approved."
                );
            } else if (request.getFeatureType().equals(FeatureType.MANAGEMENT_FEE)) {
                throw new IllegalStateException(
                        "Transaction for ManagementFee with referenceId " + request.getReferenceId() + " has been approved."
                );
            } else if (request.getFeatureType().equals(FeatureType.TAX_BROKER_FEE)) {
                throw new IllegalStateException(
                        "Transaction for TaxBrokerFee with referenceId " + request.getReferenceId() + " has been approved."
                );
            }
        }
        NcbsRequest ncbsRequest = NcbsRequest.builder()
                .createdDate(request.getCreatedDate())
                .siReferenceId(request.getSiReferenceId())
                .transferScope(request.getTransferScope())
                .processType(request.getProcessType())
                .transferMethod(request.getTransferMethod())
                .jsonRequest(request.getRequestJson())
                .referenceId(request.getReferenceId())
                .service(request.getServiceType())
                .payUserRefNo(request.getPayUserRefNo())
                .build();

        return ncbsRequestRepository.save(ncbsRequest);
    }

    // ================== HELPER ========================
    private boolean isReferenceIdAlreadyExists(String referenceId) {
        return ncbsRequestRepository.existsByReferenceId(referenceId);
    }
}
