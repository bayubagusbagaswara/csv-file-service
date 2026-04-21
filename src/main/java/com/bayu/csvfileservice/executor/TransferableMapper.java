package com.bayu.csvfileservice.executor;

import com.bayu.csvfileservice.model.ManagementFeeMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransferableMapper {

    public Transferable fromManagementFeeMap(ManagementFeeMap e) {
        TransferableAdapter adapter = TransferableAdapter.builder()
                .transferMethod(e.getTransferMethod())
                .transferScope(e.getTransferScope())
                .featureType(e.getFeatureType())
                .processType(e.getProcessType())
                .mappingStatus(e.getStatus())

                .id(e.getId())
                .debitAccount(e.getDebitAccount())
                .creditAccount(e.getCreditAccount())
                .amount(e.getAmount())
                .description(e.getDescription())
                .build();

        log.info("TransferableMapper fromManagementFeeMap: {}", adapter);
        return adapter;
    }

}
