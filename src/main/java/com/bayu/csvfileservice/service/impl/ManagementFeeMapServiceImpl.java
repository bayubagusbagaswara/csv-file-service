package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.managementfee.CreateTransactionRequest;
import com.bayu.csvfileservice.exception.DataNotFoundException;
import com.bayu.csvfileservice.executor.TransferOrchestratorService;
import com.bayu.csvfileservice.executor.Transferable;
import com.bayu.csvfileservice.executor.TransferableMapper;
import com.bayu.csvfileservice.model.*;
import com.bayu.csvfileservice.model.enumerator.*;
import com.bayu.csvfileservice.repository.*;
import com.bayu.csvfileservice.service.ManagementFeeMapService;
import com.bayu.csvfileservice.util.BankCodeHelper;
import com.bayu.csvfileservice.util.TransferMethodValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManagementFeeMapServiceImpl implements ManagementFeeMapService {

    private final ManagementFeeRawRepository rawRepository;
    private final ManagementFeeMapRepository mapRepository;
    private final MasterBankRepository masterBankRepository;
    private final DebitAccountProductRepository debitRepository;
    private final NcbsRequestRepository ncbsRequestRepository;
    private final NcbsResponseRepository ncbsResponseRepository;
    private final BankCodeHelper bankHelper;
    private final TransferMethodValidator validator;
    private final ResponseCodeRepository responseCodeRepository;
    private final TransactionLimitRepository transactionLimitRepository;
    private final TransferableMapper transferableMapper;
    private final TransferOrchestratorService orchestratorService;


    @Override
    @Transactional
    public ProcessResult map(Month month, Integer year, String clientIp) {
        ProcessResult result = new ProcessResult();

        mapRepository.deleteByMonthAndYearAndStatus(month, year, MappingStatus.DRAFT);

        List<ManagementFeeRaw> feeRawList = rawRepository.findAllByMonthAndYear(month, year);

        if (feeRawList.isEmpty()) {
            log.warn("No raw data found for {} {}", month, year);
            return result;
        }

        for (ManagementFeeRaw raw : feeRawList) {
            try {
                validateRaw(raw);

                // ================= Check by Reference Combination ===============
                if (mapRepository.existsByReferenceCombination(raw.getReferenceCombination())) {
                    throw new IllegalStateException(
                            "ManagementFee has been mapped with fundCode " + raw.getFundCode() + ", month " + raw.getMonth().getLabel()
                            + ", year " + raw.getYear() + ", amount " + raw.getAmount()
                    );
                }

                // ================= SPLIT BANK =================
                String rawCode = raw.getBankCode();

                String bankCode = bankHelper.extractBankCode(rawCode);
                String branchCode = bankHelper.extractBranchCode(rawCode);
                String formatted = bankHelper.formatBankCode(bankCode);

                // ================= MASTER BANK =================
                MasterBank bank = masterBankRepository
                        .findByBankCode(formatted)
                        .orElseThrow(() -> new DataNotFoundException("MasterBank not found: " + formatted));

                // ================= TRANSFER SCOPE =================
                TransferScope scope = bankHelper.resolveScope(branchCode);

                // ================= DEBIT ACCOUNT =================
                DebitAccountProduct dap = debitRepository.findByFundCode(raw.getFundCode())
                        .orElseThrow(() -> new DataNotFoundException("DebitAccountProduct not found: " + raw.getFundCode()));


                // ================= BUILD MAP =================
                ManagementFeeMap map = ManagementFeeMap.builder()
                        .fundCode(raw.getFundCode())
                        .month(raw.getMonth())
                        .year(raw.getYear())
                        .mutualFundName(raw.getMutualFundName())
                        .investmentManager(raw.getInvestmentManager())
                        .amount(raw.getAmount())
                        .creditAccount(raw.getCreditAccount())
                        .beneficiaryName(raw.getBeneficiaryName())
                        .bankName(raw.getBankName())
                        .paymentInstructions(raw.getPaymentInstructions())
                        .paymentType(raw.getPaymentType())
                        .period(raw.getPeriod())
                        .description(raw.getDescription())
                        // normalized
                        .bankCode(bank.getBankCode())
                        .branchCode(branchCode)
                        // enrichment
                        .biCode(bank.getBiCode())
                        .transferScope(scope)
                        .debitAccount(dap.getCashAccount())
                        .status(MappingStatus.DRAFT)
                        .referenceCombination(raw.getReferenceCombination())
                        .build();

                mapRepository.save(map);
                result.addSuccess();
            } catch (Exception e) {
                log.error("Mapping failed fundCode {}", raw.getFundCode(), e);
                result.addError(ErrorDetail.of(
                                "fundCode",
                                raw.getFundCode(),
                                List.of(e.getMessage())));
            }
        }
        return result;
    }

    // ================= CREATE TRANSACTION =================

    @Override
    @Transactional
    public ProcessResult createTransactions(List<CreateTransactionRequest> items, String clientIp) {
        ProcessResult result = new ProcessResult();
        for (CreateTransactionRequest item : items) {
            try {
                ManagementFeeMap entity = mapRepository.findById(item.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Data not found"));

                if (entity.getStatus() != MappingStatus.DRAFT) {
                    throw new IllegalStateException("Only DRAFT data can be processed");
                }

                // VALIDASI METHOD
                validator.validate(
                        entity.getTransferScope(),
                        item.getTransferMethod()
                );

                // =============== Check range amount ===============
                BigDecimal amount = entity.getAmount();
                TransactionLimit transactionLimit = transactionLimitRepository.findByTransferMethod(item.getTransferMethod())
                        .orElseThrow(() -> new DataNotFoundException("Transfer method " + item.getTransferMethod().getName() + " is not defined"));
                if (amount.compareTo(transactionLimit.getMinAmount()) < 0
                        || amount.compareTo(transactionLimit.getMaxAmount()) > 0) {
                    throw new IllegalArgumentException("Amount " + amount + " is out of range for transfer method " + item.getTransferMethod().getName());
                }

                // =============== Set Transfer Method to Entity ================
                entity.setTransferMethod(item.getTransferMethod());
                entity.setStatus(MappingStatus.READY);

                // =============== Save entity ==================
                mapRepository.save(entity);
                result.addSuccess();

            } catch (Exception e) {
                result.addError(
                        ErrorDetail.of(
                                "id",
                                String.valueOf(item.getId()),
                                List.of(e.getMessage())
                        )
                );
            }
        }
        return result;
    }

    @Override
    public ProcessResult sendTransactions(List<Long> ids, String clientIp) {
        ProcessResult result = new ProcessResult();
        List<ManagementFeeMap> list = mapRepository.findAllById(ids);

        for (ManagementFeeMap entity : list) {
            try {
                // ============= VALIDATION ==================
                if (!MappingStatus.READY.equals(entity.getStatus())
                        && !MappingStatus.RETRY.equals(entity.getStatus())) {
                    throw new IllegalStateException("Data with id " + entity.getId() + " cannot be sent");
                }

                // ================= Set SENT =============
                entity.setStatus(MappingStatus.SENT);
                entity.setLastSentDate(LocalDateTime.now());
                entity.setRetryCount(
                        entity.getRetryCount() == null ? 1 : entity.getRetryCount() + 1
                );

                mapRepository.save(entity);

                // ============= EXECUTE ==================
                Transferable transferable = transferableMapper.fromManagementFeeMap(entity);
                log.info("ManagementFeeMapService transferableMapper fromManagementFeeMap: {}", transferable);

                NcbsResponse ncbsResponse = orchestratorService.execute(transferable);
                log.info("ManagementFeeMapService orchestratorService execute: {}", ncbsResponse);

                // =========== Set referenceId to entity ===========
                entity.setReferenceId(ncbsResponse.getReferenceId());

                if (ncbsResponse.getResponseCode().equalsIgnoreCase(ApiResponseCode.SUCCESS.getCode())) {
                     entity.setStatus(MappingStatus.SUCCESS);
                } else if (checkIfInsufficientBalance(ncbsResponse.getResponseCode())) {
                    entity.setStatus(MappingStatus.RETRY);
                } else {
                    entity.setStatus(MappingStatus.FAILED);
                }

                // ============ Save entity to update status ===============
                mapRepository.save(entity);
                result.addSuccess();

            } catch (Exception e) {
                log.error("Send failed id {}", entity.getId(), e);
                result.addError(
                        ErrorDetail.of(
                                "id",
                                String.valueOf(entity.getId()),
                                List.of("Send failed " + e.getMessage())
                        )
                );
            }
        }
        return result;
    }

    // ======================= HELPER =======================

    private void validateRaw(ManagementFeeRaw raw) {
        if (raw.getBankCode() == null || raw.getBankCode().length() < 7) {
            throw new IllegalArgumentException("Invalid bankCode format");
        }

        if (raw.getFundCode() == null) {
            throw new IllegalArgumentException("FundCode is required");
        }
    }


    private boolean checkIfInsufficientBalance(String responseCode) {
        List<String> list = responseCodeRepository.findAllByName(NcbsStatus.INSUFFICIENT_BALANCE.name()).stream()
                .map(ResponseCode::getCode)
                .toList();
        return list.contains(responseCode);
    }

}
