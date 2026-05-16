package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.transaction.*;
import com.bayu.csvfileservice.model.DepositTransferMap;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.service.DepositTransferTransactionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class DepositTransferTransactionServiceImpl implements DepositTransferTransactionService {

    @Override
    public ProcessResult createSingleTransaction(CreateSingleDepositTransferTransactionRequest request, String userId, String clientIp) {
        return null;
    }

    @Override
    public ProcessResult createBulkTransaction(CreateBulkDepositTransferTransactionRequest request, String userId, String clientIp) {
        return null;
    }

    @Override
    public ProcessResult sendTransaction(SendDepositTransferTransactionRequest request, String userId, String clientIp) {
        return null;
    }

    @Override
    public ProcessResult rejectTransaction(RejectDepositTransferTransactionRequest request, String userId, String clientIp) {
        return null;
    }

    @Override
    public List<DepositTransferTransactionDto> getAllTransactionByCurrentDate(LocalDate currentDate) {
        return List.of();
    }


    private void validateSendStatus(MappingStatus mappingStatus) {
        boolean canSend = MappingStatus.READY.equals(mappingStatus)
                || MappingStatus.RETRY.equals(mappingStatus);
        if (!canSend) {
            throw new IllegalStateException("Only READY or RETRY data can be sent");
        }
    }

    private static String buildJoinedSiReferenceId(List<DepositTransferMap> list) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }

            builder.append(list.get(i).getSiReferenceId());
        }
        return builder.toString();
    }

    private static String createBulkReferenceId() {
        String dateTime = ZonedDateTime.now(ZoneId.of("Asia/Jakarta"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return dateTime + "_" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                ;
    }

    private static void validateBulkCandidate(List<DepositTransferMap> candidateList) {
        if (candidateList == null || candidateList.isEmpty()) {
            throw new IllegalArgumentException("DepositTransferMap list must be not be empty");
        }

        // Use first record as reference (baseline)
        DepositTransferMap reference = candidateList.get(0);

        String referenceFundCode = reference.getFundCode();
        String referenceCashAccountNo = reference.getCashAccountNo();
        String referenceBankCode = reference.getBankCode();

        for (DepositTransferMap data : candidateList) {

            // Validate status
            if (!MappingStatus.DRAFT.equals(data.getMappingStatus())) {
                throw new IllegalStateException(
                        "Bulk operation is rejected: only records with DRAFT status are allowed."
                );
            }

            // validate all required fields must be identical
            if (!Objects.equals(referenceFundCode, data.getFundCode())
                    || !Objects.equals(referenceCashAccountNo, data.getCashAccountNo())
                    || !Objects.equals(referenceBankCode, data.getBankCode())
            ) {
                throw new IllegalStateException(
                        "Bulk operation is rejected due to inconsistent data. " +
                                "fundCode= " + data.getFundCode() +
                                ", cashAccountNo= " + data.getCashAccountNo() +
                                ", bankCode= " + data.getBankCode()
                );
            }
        }
    }

}
