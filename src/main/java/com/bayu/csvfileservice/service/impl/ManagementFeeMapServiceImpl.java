package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.exception.DataNotFoundException;
import com.bayu.csvfileservice.executor.TransferOrchestratorService;
import com.bayu.csvfileservice.executor.TransferableMapper;
import com.bayu.csvfileservice.model.*;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.Month;
import com.bayu.csvfileservice.model.enumerator.NcbsStatus;
import com.bayu.csvfileservice.model.enumerator.TransferScope;
import com.bayu.csvfileservice.repository.*;
import com.bayu.csvfileservice.service.ManagementFeeMapService;
import com.bayu.csvfileservice.util.BankCodeHelper;
import com.bayu.csvfileservice.util.TransferMethodValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        List<ManagementFee> feeRawList = rawRepository.findAllByMonthAndYear(month, year);

        if (feeRawList.isEmpty()) {
            log.warn("No raw data found for {} {}", month, year);
            return result;
        }

        for (ManagementFee raw : feeRawList) {
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


    // ======================= HELPER =======================

    private void validateRaw(ManagementFee raw) {
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
