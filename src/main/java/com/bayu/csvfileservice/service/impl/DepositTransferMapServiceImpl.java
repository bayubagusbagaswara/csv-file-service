package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.map.DepositTransferMapDto;
import com.bayu.csvfileservice.exception.DataNotFoundException;
import com.bayu.csvfileservice.executor.TransferOrchestratorService;
import com.bayu.csvfileservice.executor.TransferableMapper;
import com.bayu.csvfileservice.mapper.DepositTransferMapper;
import com.bayu.csvfileservice.model.DebitAccountProduct;
import com.bayu.csvfileservice.model.DepositTransferMap;
import com.bayu.csvfileservice.model.MasterBank;
import com.bayu.csvfileservice.model.SInvest;
import com.bayu.csvfileservice.model.enumerator.*;
import com.bayu.csvfileservice.repository.DebitAccountProductRepository;
import com.bayu.csvfileservice.repository.DepositTransferMapRepository;
import com.bayu.csvfileservice.repository.MasterBankRepository;
import com.bayu.csvfileservice.repository.SInvestRepository;
import com.bayu.csvfileservice.service.DepositTransferMapService;
import com.bayu.csvfileservice.service.ResponseCodeService;
import com.bayu.csvfileservice.util.TransferMethodValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DepositTransferMapServiceImpl implements DepositTransferMapService {

    private static final String SI_REFERENCE_ID = "siReferenceId";

    private final SInvestRepository sInvestRepository;
    private final DepositTransferMapRepository depositTransferMapRepository;
    private final DebitAccountProductRepository debitAccountProductRepository;
    private final MasterBankRepository masterBankRepository;
    private final DepositTransferMapper depositTransferMapper;
    private final TransferMethodValidator transferMethodValidator;
    private final TransferableMapper transferableMapper;
    private final TransferOrchestratorService transferOrchestratorService;
    private final ResponseCodeService responseCodeService;

    public DepositTransferMapServiceImpl(SInvestRepository sInvestRepository, DepositTransferMapRepository depositTransferMapRepository, DebitAccountProductRepository debitAccountProductRepository, MasterBankRepository masterBankRepository, DepositTransferMapper depositTransferMapper, TransferMethodValidator transferMethodValidator, TransferableMapper transferableMapper, TransferOrchestratorService transferOrchestratorService, ResponseCodeService responseCodeService) {
        this.sInvestRepository = sInvestRepository;
        this.depositTransferMapRepository = depositTransferMapRepository;
        this.debitAccountProductRepository = debitAccountProductRepository;
        this.masterBankRepository = masterBankRepository;
        this.depositTransferMapper = depositTransferMapper;
        this.transferMethodValidator = transferMethodValidator;
        this.transferableMapper = transferableMapper;
        this.transferOrchestratorService = transferOrchestratorService;
        this.responseCodeService = responseCodeService;
    }

    @Override
    public ProcessResult map(LocalDate currentDate, String userId, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult processResult = new ProcessResult();

        // 1. Ambil semua data S-Invest berdasarkan tanggal.
        // Data ini adalah raw data hasil upload user.
        List<SInvest> sInvests = sInvestRepository.findAllByDate(currentDate);

        // 2. Hitung jumlah kemunculan siReferenceId pada tanggal yang sama.
        // Jika siReferenceId muncul lebih dari 1 kali, maka hasil mapping akan HOLD.
        Map<String, Long> duplicateCounter = sInvests.stream()
                .filter(item -> item.getSiReferenceId() != null)
                .collect(Collectors.groupingBy(
                        SInvest::getSiReferenceId,
                        Collectors.counting()
                ));

        // 3. Loop semua data S-Invest untuk diproses menjadi DepositTransferMap.
        for (SInvest sInvest : sInvests) {

            String siReferenceId = sInvest.getSiReferenceId();

            try {

                // 4. Validasi siReferenceId wajib ada.
                if (siReferenceId == null || siReferenceId.trim().isEmpty()) {
                    throw new IllegalArgumentException("siReferenceId is required");
                }

                // 5. Cek apakah siReferenceId duplicate pada tanggal yang sama.
                boolean duplicateInSameDate = duplicateCounter.get(siReferenceId) != null
                        && duplicateCounter.get(siReferenceId) > 1;

                // 6. Cek apakah siReferenceId sudah masuk transaksi aktif.
                //
                // Karena READY, SENT, SUCCESS, dan RETRY sekarang adalah TransactionStatus,
                // maka pengecekan dilakukan melalui relasi:
                //
                // DepositTransferMap -> DepositTransferTransaction
                boolean alreadyExistsAsActiveTransaction =
                        depositTransferMapRepository.existsActiveTransactionBySiReferenceId(
                                siReferenceId,
                                Arrays.asList(
                                        TransactionStatus.READY,
                                        TransactionStatus.SENT,
                                        TransactionStatus.SUCCESS,
                                        TransactionStatus.RETRY
                                )
                        );

                // 7. Jika sudah masuk transaksi aktif, maka tidak boleh dimapping ulang.
                if (alreadyExistsAsActiveTransaction) {
                    processResult.addError(
                            ErrorDetail.of(
                                    SI_REFERENCE_ID,
                                    siReferenceId,
                                    Collections.singletonList(
                                            "Data already exists in active Deposit Transfer transaction"
                                    )
                            )
                    );
                    continue;
                }

                // 8. Jika data lama masih DRAFT atau HOLD,
                // maka boleh direplace karena belum menjadi transaksi aktif.
                depositTransferMapRepository.deleteBySiReferenceIdAndMappingStatusIn(
                        siReferenceId,
                        Arrays.asList(
                                MappingStatus.DRAFT,
                                MappingStatus.HOLD
                        )
                );

                // 9. Mapping field dasar dari SInvest ke DepositTransferMap.
                DepositTransferMap depositTransferMap =
                        depositTransferMapper.fromSInvestToDepositTransferMap(sInvest);

                // 10. Lookup data debit account berdasarkan fundCode.
                DebitAccountProduct debitAccountProduct = debitAccountProductRepository
                        .findByFundCode(sInvest.getFundCode())
                        .orElseThrow(() -> new DataNotFoundException(
                                "DebitAccountProduct not found for fundCode: " + sInvest.getFundCode()
                        ));

                // 11. Lookup data bank berdasarkan bankCode.
                MasterBank masterBank = masterBankRepository
                        .findByBankCode(sInvest.getBankCode())
                        .orElseThrow(() -> new DataNotFoundException(
                                "MasterBank not found for bankCode: " + sInvest.getBankCode()
                        ));

                // 12. Set data hasil lookup dari DebitAccountProduct.
                depositTransferMap.setAccountDebitNo(debitAccountProduct.getCashAccount());
                depositTransferMap.setProductCode(debitAccountProduct.getProductCode());

                // 13. Set data hasil lookup dari MasterBank.
                depositTransferMap.setBiCode(masterBank.getBiCode());
                depositTransferMap.setBankType(masterBank.getBankType());
                depositTransferMap.setBranchCode(masterBank.getBranchCode());

                // 14. Tentukan transferScope.
                // Jika bankCode = 0011, maka INTERNAL.
                // Selain itu EXTERNAL.
                depositTransferMap.setTransferScope(resolveTransferScope(sInvest.getBankCode()));

                // 15. Pada tahap map, data belum masuk transaction.
                depositTransferMap.setTransactionStatus(null);

                // 16. Set audit input.
                depositTransferMap.setInputId(userId);
                depositTransferMap.setInputDate(now);
                depositTransferMap.setInputIpAddress(clientIp);

                // 17. Karena proses mapping tidak memakai maker-checker,
                // maka approvalStatus langsung APPROVED.
                depositTransferMap.setApprovalStatus(ApprovalStatus.APPROVED);
                depositTransferMap.setApproveId(userId);
                depositTransferMap.setApproveDate(now);
                depositTransferMap.setApproveIpAddress(clientIp);

                // 18. Tentukan MappingStatus.
                //
                // Duplicate di tanggal yang sama -> HOLD.
                // Normal -> DRAFT.
                if (duplicateInSameDate) {
                    depositTransferMap.setMappingStatus(MappingStatus.HOLD);
                    depositTransferMap.setDescription(
                            "Possible duplicate siReferenceId on the same date"
                    );
                } else {
                    depositTransferMap.setMappingStatus(MappingStatus.DRAFT);
                    depositTransferMap.setDescription(sInvest.getFundCode());
                }

                // 19. Simpan hasil mapping.
                depositTransferMapRepository.save(depositTransferMap);

                processResult.addSuccess();

            } catch (Exception e) {
                log.error("Failed mapping SInvest data. siReferenceId={}", siReferenceId, e);
                processResult.addError(
                        ErrorDetail.of(
                                SI_REFERENCE_ID,
                                siReferenceId,
                                Collections.singletonList(e.getMessage())
                        )
                );
            }
        }

        return processResult;
    }

    @Override
    public List<DepositTransferMapDto> getAllByCurrentDate(LocalDate currentDate) {
        return List.of();
    }

    @Override
    public List<DepositTransferMapDto> getAllByCurrentDateAndMappingStatus(LocalDate currentDate, MappingStatus mappingStatusEnum) {
        return List.of();
    }

    @Override
    public List<DepositTransferMapDto> getAllByCurrentDateAndApprovalStatusAndTransferMethod(LocalDate currentDate, ApprovalStatus approvalStatusEnum, TransferMethod transferMethodEnum) {
        return List.of();
    }

    @Override
    public ProcessResult releaseHold(List<Long> ids, String releaseBy, String clientIp) {
        // releaseBy = userId

        return null;
    }

    // ======================== HELPER =============================
    private TransferScope resolveTransferScope(String bankCode) {
        return "0011".equals(bankCode)
                ? TransferScope.INTERNAL
                : TransferScope.EXTERNAL;
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
