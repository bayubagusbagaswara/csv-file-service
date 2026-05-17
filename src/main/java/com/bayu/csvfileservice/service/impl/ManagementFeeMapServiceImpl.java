package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.exception.DataNotFoundException;
import com.bayu.csvfileservice.model.DebitAccountProduct;
import com.bayu.csvfileservice.model.ManagementFee;
import com.bayu.csvfileservice.model.ManagementFeeMap;
import com.bayu.csvfileservice.model.MasterBank;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.Month;
import com.bayu.csvfileservice.model.enumerator.TransferScope;
import com.bayu.csvfileservice.repository.DebitAccountProductRepository;
import com.bayu.csvfileservice.repository.ManagementFeeMapRepository;
import com.bayu.csvfileservice.repository.ManagementFeeRepository;
import com.bayu.csvfileservice.repository.MasterBankRepository;
import com.bayu.csvfileservice.service.ManagementFeeMapService;
import com.bayu.csvfileservice.util.BankCodeHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManagementFeeMapServiceImpl implements ManagementFeeMapService {

    private static final String PERIOD = "period";
    private static final String FUND_CODE = "fundCode";
    private static final String REFERENCE_COMBINATION = "referenceCombination";

    private final ManagementFeeRepository managementFeeRepository;
    private final ManagementFeeMapRepository managementFeeMapRepository;

    private final MasterBankRepository masterBankRepository;
    private final DebitAccountProductRepository debitAccountProductRepository;

    private final BankCodeHelper bankHelper;

    @Override
    @Transactional
    public ProcessResult map(Month month, Integer year, String clientIp) {
        ProcessResult result = new ProcessResult();

        // 1. Validasi parameter periode.
        // Jika month/year tidak valid, exception akan dilempar ke GlobalExceptionHandler.
        validateMapRequest(month, year);

        // 2. Ambil data mentah ManagementFee berdasarkan month dan year.
        List<ManagementFee> rawList = managementFeeRepository.findAllByMonthAndYear(month, year);

        // 3. Jika raw data kosong, proses selesai.
        if (rawList.isEmpty()) {
            log.warn("No Management Fee raw data found. month={}, year={}", month, year);
            result.addError(
                    ErrorDetail.of(
                            PERIOD,
                            month + " " + year,
                            Collections.singletonList("No Management Fee raw data found. month=" + month + ", year=" + year)));
            return result;
        }

        // 4. Hitung duplicate referenceCombination dalam periode tersebut.
        Map<String, Long> duplicateCounter = rawList.stream()
                .filter(item -> item.getReferenceCombination() != null)
                .collect(Collectors.groupingBy(
                        ManagementFee::getReferenceCombination,
                        Collectors.counting()
                ));

        // 5. Loop data raw.
        for (ManagementFee raw : rawList) {

            String referenceCombination = raw.getReferenceCombination();

            try {
                // 6. Validasi field minimal untuk mapping.
                validateRaw(raw);

                // 7. Validasi referenceCombination.
                if (referenceCombination == null || referenceCombination.trim().isEmpty()) {
                    throw new IllegalArgumentException("referenceCombination is required");
                }

                // 8. Cek duplicate referenceCombination dalam periode yang sama.
                boolean duplicateInSamePeriod = duplicateCounter.get(referenceCombination) != null
                        && duplicateCounter.get(referenceCombination) > 1;

                if (duplicateInSamePeriod) {
                    throw new IllegalStateException(
                            "Duplicate Management Fee detail detected for referenceCombination: "
                                    + referenceCombination
                    );
                }

                // 9. Cek apakah referenceCombination sudah masuk transaksi aktif.
                boolean alreadyExistsAsActiveData =
                        managementFeeMapRepository.existsByReferenceCombinationAndStatusIn(
                                referenceCombination,
                                Arrays.asList(
                                        MappingStatus.READY,
                                        MappingStatus.SENT,
                                        MappingStatus.SUCCESS,
                                        MappingStatus.RETRY
                                )
                        );

                if (alreadyExistsAsActiveData) {
                    result.addError(
                            ErrorDetail.of(
                                    REFERENCE_COMBINATION,
                                    referenceCombination,
                                    Collections.singletonList(
                                            "Management Fee already exists with active transaction status"
                                    )
                            )
                    );
                    continue;
                }

                // 10. Jika data lama masih DRAFT dengan referenceCombination yang sama,
                // maka boleh direplace dengan hasil mapping terbaru.
                managementFeeMapRepository.deleteByReferenceCombinationAndStatus(
                        referenceCombination,
                        MappingStatus.DRAFT
                );

                // 11. Parse raw bankCode.
                String rawBankCode = raw.getBankCode();

                String extractedBankCode = bankHelper.extractBankCode(rawBankCode);
                String branchCode = bankHelper.extractBranchCode(rawBankCode);
                String formattedBankCode = bankHelper.formatBankCode(extractedBankCode);

                // 12. Lookup MasterBank.
                MasterBank masterBank = masterBankRepository
                        .findByBankCode(formattedBankCode)
                        .orElseThrow(() -> new DataNotFoundException(
                                "MasterBank not found with bankCode: " + formattedBankCode
                        ));

                // 13. Resolve TransferScope.
                TransferScope transferScope = bankHelper.resolveScope(branchCode);

                // 14. Lookup DebitAccountProduct.
                DebitAccountProduct debitAccountProduct = debitAccountProductRepository
                        .findByFundCode(raw.getFundCode())
                        .orElseThrow(() -> new DataNotFoundException(
                                "DebitAccountProduct not found with fundCode: " + raw.getFundCode()
                        ));

                // 15. Build ManagementFeeMap.
                ManagementFeeMap map = ManagementFeeMap.builder()
                        .month(raw.getMonth())
                        .year(raw.getYear())
                        .mutualFundName(raw.getMutualFundName())
                        .investmentManager(raw.getInvestmentManager())
                        .fundCode(raw.getFundCode())

                        .debitAccount(debitAccountProduct.getCashAccount())
                        .amount(raw.getAmount())
                        .creditAccount(raw.getCreditAccount())
                        .beneficiaryName(raw.getBeneficiaryName())

                        .bankName(raw.getBankName())
                        .paymentInstructions(raw.getPaymentInstructions())
                        .paymentType(raw.getPaymentType())
                        .period(raw.getPeriod())
                        .description(raw.getDescription())

                        .bankCode(masterBank.getBankCode())
                        .branchCode(branchCode)
                        .biCode(masterBank.getBiCode())
                        .transferScope(transferScope)

                        .transferMethod(null)
                        .referenceId(null)
                        .inquiryReferenceId(null)
                        .retryCount(0)
                        .lastSentDate(null)

                        .status(MappingStatus.DRAFT)
                        .referenceCombination(referenceCombination)
                        .build();

                // 16. Simpan hasil mapping.
                managementFeeMapRepository.save(map);

                result.addSuccess();

            } catch (Exception e) {
                log.error(
                        "Failed to map Management Fee. referenceCombination={}, fundCode={}, month={}, year={}",
                        referenceCombination,
                        raw.getFundCode(),
                        raw.getMonth(),
                        raw.getYear(),
                        e
                );

                result.addError(
                        ErrorDetail.of(
                                "referenceCombination",
                                referenceCombination,
                                Collections.singletonList(e.getMessage())
                        )
                );
            }
        }

        return result;
    }


    // ======================= HELPER =======================

    private void validateMapRequest(Month month, Integer year) {
        if (month == null) {
            throw new IllegalArgumentException("Month is required");
        }
        if (year == null) {
            throw new IllegalArgumentException("Year is required");
        }
    }

    private void validateRaw(ManagementFee raw) {
        if (raw.getBankCode() == null || raw.getBankCode().length() < 7) {
            throw new IllegalArgumentException("Invalid bankCode format");
        }

        if (raw.getFundCode() == null) {
            throw new IllegalArgumentException("FundCode is required");
        }
    }

}
