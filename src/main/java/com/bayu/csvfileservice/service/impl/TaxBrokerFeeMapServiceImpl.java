package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.taxbrokerfee.TaxBrokerFeeMapDto;
import com.bayu.csvfileservice.mapper.TaxBrokerFeeMapMapper;
import com.bayu.csvfileservice.model.TaxBrokerFee;
import com.bayu.csvfileservice.model.TaxBrokerFeeMap;
import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.TransferScope;
import com.bayu.csvfileservice.repository.TaxBrokerFeeMapRepository;
import com.bayu.csvfileservice.repository.TaxBrokerFeeRepository;
import com.bayu.csvfileservice.service.TaxBrokerFeeMapService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaxBrokerFeeMapServiceImpl implements TaxBrokerFeeMapService {

    private static final String REFERENCE_COMBINATION = "referenceCombination";

    private final TaxBrokerFeeRepository taxBrokerFeeRepository;
    private final TaxBrokerFeeMapRepository taxBrokerFeeMapRepository;
    private final TaxBrokerFeeMapMapper taxBrokerFeeMapMapper;

    @Override
    @Transactional
    public ProcessResult map(
            LocalDate date,
            String userId,
            String clientIp
    ) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult result = new ProcessResult();

        // 1. Validasi tanggal.
        validateMapRequest(date);

        // 2. Ambil raw data TaxBrokerFee berdasarkan date.
        List<TaxBrokerFee> rawList = taxBrokerFeeRepository.findAllByDate(date);

        if (rawList.isEmpty()) {
            log.warn("No Tax Broker Fee raw data found. date={}", date);
            return result;
        }

        // 3. Hitung duplicate referenceCombination pada tanggal tersebut.
        Map<String, Long> duplicateCounter = rawList.stream()
                .filter(item -> item.getReferenceCombination() != null)
                .collect(Collectors.groupingBy(
                        TaxBrokerFee::getReferenceCombination,
                        Collectors.counting()
                ));

        // 4. Loop raw data untuk dibuat ke TaxBrokerFeeMap.
        for (TaxBrokerFee raw : rawList) {

            String referenceCombination = raw.getReferenceCombination();

            try {
                // 5. Validasi field minimum.
                validateRaw(raw);

                // 6. Validasi referenceCombination.
                if (referenceCombination == null || referenceCombination.trim().isEmpty()) {
                    throw new IllegalArgumentException("referenceCombination is required");
                }

                // 7. Cek duplicate referenceCombination pada tanggal yang sama.
                boolean duplicateInSameDate =
                        duplicateCounter.get(referenceCombination) != null
                                && duplicateCounter.get(referenceCombination) > 1;

                if (duplicateInSameDate) {
                    throw new IllegalStateException(
                            "Duplicate Tax Broker Fee detail detected for referenceCombination: "
                                    + referenceCombination
                    );
                }

                // 8. Cek apakah referenceCombination sudah masuk transaksi aktif.
                boolean alreadyExistsAsActiveData =
                        taxBrokerFeeMapRepository.existsByReferenceCombinationAndStatusIn(
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
                                            "Tax Broker Fee already exists with active transaction status"
                                    )
                            )
                    );
                    continue;
                }

                // 9. Jika data lama masih DRAFT, boleh direplace.
                taxBrokerFeeMapRepository.deleteByReferenceCombinationAndStatus(
                        referenceCombination,
                        MappingStatus.DRAFT
                );

                // 10. Build TaxBrokerFeeMap.
                TaxBrokerFeeMap map = TaxBrokerFeeMap.builder()
                        .date(raw.getDate())
                        .portfolioCode(raw.getPortfolioCode())
                        .name(raw.getName())
                        .investmentManager(raw.getInvestmentManager())

                        .debitAccount(raw.getDebitAccount())
                        .creditAccount(raw.getCreditAccount())
                        .creditAccountName(raw.getCreditAccountName())

                        .biCode(raw.getBiCode())
                        .beneficiaryBankName(raw.getBeneficiaryBankName())

                        .type(raw.getType())
                        .totalRvp(raw.getTotalRvp())
                        .totalDvp(raw.getTotalDvp())
                        .netAmount(raw.getNetAmount())
                        .taxAmount(raw.getTaxAmount())
                        .description(raw.getDescription())

                        .transferScope(resolveTransferScope(raw))
                        .transferMethod(null)

                        .mappingStatus(MappingStatus.DRAFT)

                        .referenceId(null)
                        .inquiryReferenceId(null)
                        .retryCount(0)
                        .lastSentDate(null)

                        .referenceCombination(referenceCombination)

                        // mapping tidak pakai maker-checker
                        .inputId(userId)
                        .inputDate(now)
                        .inputIpAddress(clientIp)
                        .approvalStatus(ApprovalStatus.APPROVED)
                        .approveId(userId)
                        .approveDate(now)
                        .approveIpAddress(clientIp)
                        .build();

                // 11. Simpan hasil mapping.
                taxBrokerFeeMapRepository.save(map);

                result.addSuccess();

            } catch (Exception e) {
                log.error(
                        "Failed to map Tax Broker Fee. referenceCombination={}, portfolioCode={}, date={}",
                        referenceCombination,
                        raw.getPortfolioCode(),
                        raw.getDate(),
                        e
                );

                result.addError(
                        ErrorDetail.of(
                                REFERENCE_COMBINATION,
                                referenceCombination,
                                Collections.singletonList(e.getMessage())
                        )
                );
            }
        }

        return result;
    }

    @Override
    public List<TaxBrokerFeeMapDto> getAllByDate(LocalDate date) {

        if (date == null) {
            throw new IllegalArgumentException("date is required");
        }

        List<TaxBrokerFeeMap> entities =
                taxBrokerFeeMapRepository.findAllByDate(date);

        return taxBrokerFeeMapMapper.toDtos(entities);
    }

    @Override
    public List<TaxBrokerFeeMapDto> getAllByDateAndStatus(
            LocalDate date,
            MappingStatus status
    ) {

        if (date == null) {
            throw new IllegalArgumentException("date is required");
        }

        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }

        List<TaxBrokerFeeMap> entities =
                taxBrokerFeeMapRepository.findAllByDateAndStatus(date, status);

        return taxBrokerFeeMapMapper.toDtos(entities);
    }

    private void validateMapRequest(LocalDate date) {

        if (date == null) {
            throw new IllegalArgumentException("date is required");
        }
    }

    private void validateRaw(TaxBrokerFee raw) {

        if (raw == null) {
            throw new IllegalArgumentException("TaxBrokerFee raw data is required");
        }

        if (raw.getDebitAccount() == null || raw.getDebitAccount().trim().isEmpty()) {
            throw new IllegalArgumentException("debitAccount is required");
        }

        if (raw.getCreditAccount() == null || raw.getCreditAccount().trim().isEmpty()) {
            throw new IllegalArgumentException("creditAccount is required");
        }

        if (raw.getReferenceCombination() == null || raw.getReferenceCombination().trim().isEmpty()) {
            throw new IllegalArgumentException("referenceCombination is required");
        }
    }

    private TransferScope resolveTransferScope(TaxBrokerFee raw) {
        // Sesuaikan dengan rule bisnis.
        // Jika TaxBrokerFee selalu overbooking/internal, gunakan INTERNAL.
        return TransferScope.INTERNAL;
    }
}
