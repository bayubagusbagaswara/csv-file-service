package com.bayu.csvfileservice.executor;

import com.bayu.csvfileservice.model.DepositTransferMap;
import com.bayu.csvfileservice.model.ManagementFeeMap;
import com.bayu.csvfileservice.model.enumerator.ProcessType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

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

    public Transferable fromDepositTransferMap(DepositTransferMap e) {

        TransferableAdapter dto = new TransferableAdapter();

        dto.setId(e.getId());
        dto.setDebitAccount(e.getAccountDebitNo());
        dto.setCreditAccount(e.getCashAccountNo());
        dto.setAmount(e.getPrinciple());
        dto.setDescription(e.getDescription());
        dto.setTransferMethod(e.getTransferMethod());
        dto.setTransferScope(e.getTransferScope());



        dto.setProcessType(e.getProcessType());
        dto.setBulkReferenceId(e.getBulkReferenceId());
        dto.setSiReferenceId(e.getSiReferenceId());

        return dto;
    }

    public Transferable fromDepositTransferBulk(List<DepositTransferMap> list) {

        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("Bulk data cannot be empty");
        }

        DepositTransferMap first = list.get(0);

        BigDecimal totalAmount = BigDecimal.ZERO;
        StringBuilder siReferenceBuilder = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            DepositTransferMap item = list.get(i);

            totalAmount = totalAmount.add(item.getPrinciple());

            if (i > 0) {
                siReferenceBuilder.append(", ");
            }

            siReferenceBuilder.append(item.getSiReferenceId());
        }

        TransferableAdapter dto = new TransferableAdapter();

        dto.setId(first.getId());
        dto.setDebitAccount(first.getAccountDebitNo());
        dto.setCreditAccount(first.getCashAccountNo());
        dto.setAmount(totalAmount);
        dto.setDescription("Bulk Deposit Transfer - SI Reference: " + siReferenceBuilder);
        dto.setTransferMethod(first.getTransferMethod());
        dto.setTransferScope(first.getTransferScope());


        dto.setProcessType(ProcessType.BULK);
        dto.setBulkReferenceId(first.getBulkReferenceId());
        dto.setSiReferenceId(siReferenceBuilder.toString());

        return dto;
    }

}
