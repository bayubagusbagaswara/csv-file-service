package com.bayu.csvfileservice.executor;

import com.bayu.csvfileservice.model.DepositTransferTransaction;
import com.bayu.csvfileservice.model.ManagementFeeMap;
import com.bayu.csvfileservice.model.enumerator.FeatureType;
import com.bayu.csvfileservice.model.enumerator.ProcessType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransferableMapper {

    public Transferable fromManagementFeeMap(ManagementFeeMap e) {

        TransferableAdapter adapter = TransferableAdapter.builder()
                .id(e.getId())
                .transferMethod(e.getTransferMethod())
                .transferScope(e.getTransferScope())
                .featureType(FeatureType.MANAGEMENT_FEE)
                .processType(e.getProcessType())
                .mappingStatus(e.getStatus())

                .debitAccount(e.getDebitAccount())
                .creditAccount(e.getCreditAccount())
                .amount(e.getAmount())
                .description(e.getDescription())

                .biCode(e.getBiCode())
                .branchCode(e.getBranchCode())

                // untuk ManagementFee, bisa pakai referenceCombination sebagai info reference
                .siReferenceId(e.getReferenceCombination())

                .bulkReferenceId(null)
                .build();

        log.info("TransferableMapper fromManagementFeeMap: {}", adapter);

        return adapter;
    }

    public Transferable fromDepositTransferTransaction(DepositTransferTransaction e) {

        TransferableAdapter adapter = TransferableAdapter.builder()
                .id(e.getId())
                .transferMethod(e.getTransferMethod())
                .transferScope(e.getTransferScope())
                .featureType(FeatureType.DEPOSIT_TRANSFER)
                .processType(e.getProcessType())

                .debitAccount(e.getAccountDebitNo())
                .creditAccount(e.getCashAccountNo())
                .amount(e.getTotalAmount())
                .description(e.getDescription())

                .biCode(e.getBiCode())
                .branchCode(e.getBranchCode())

                .bulkReferenceId(e.getBulkReferenceId())

                .siReferenceId(
                        ProcessType.BULK.equals(e.getProcessType())
                                ? e.getBulkSiReferenceIds()
                                : e.getSiReferenceId()
                )
                .build();

        log.info("TransferableMapper fromDepositTransferTransaction: {}", adapter);

        return adapter;
    }

}
