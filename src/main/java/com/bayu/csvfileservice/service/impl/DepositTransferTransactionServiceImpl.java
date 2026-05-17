package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.deposittransfer.transaction.DepositTransferTransactionDto;
import com.bayu.csvfileservice.exception.DataNotFoundException;
import com.bayu.csvfileservice.executor.TransferExecutionResult;
import com.bayu.csvfileservice.executor.TransferOrchestratorService;
import com.bayu.csvfileservice.executor.Transferable;
import com.bayu.csvfileservice.executor.TransferableAdapter;
import com.bayu.csvfileservice.mapper.DepositTransferTransactionMapper;
import com.bayu.csvfileservice.model.DepositTransferMap;
import com.bayu.csvfileservice.model.DepositTransferTransaction;
import com.bayu.csvfileservice.model.enumerator.*;
import com.bayu.csvfileservice.repository.DepositTransferMapRepository;
import com.bayu.csvfileservice.repository.DepositTransferTransactionRepository;
import com.bayu.csvfileservice.service.DepositTransferTransactionService;
import com.bayu.csvfileservice.service.ResponseCodeService;
import com.bayu.csvfileservice.util.TransferMethodValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class DepositTransferTransactionServiceImpl implements DepositTransferTransactionService {

    private static final String ID = "id";
    private static final String IDS = "ids";

    private final DepositTransferMapRepository depositTransferMapRepository;
    private final DepositTransferTransactionRepository depositTransferTransactionRepository;
    private final TransferMethodValidator transferMethodValidator;
    private final TransferOrchestratorService transferOrchestratorService;
    private final ResponseCodeService responseCodeService;
    private final DepositTransferTransactionMapper depositTransferTransactionMapper;

    public DepositTransferTransactionServiceImpl(DepositTransferMapRepository depositTransferMapRepository, DepositTransferTransactionRepository depositTransferTransactionRepository, TransferMethodValidator transferMethodValidator, TransferOrchestratorService transferOrchestratorService, ResponseCodeService responseCodeService, DepositTransferTransactionMapper depositTransferTransactionMapper) {
        this.depositTransferMapRepository = depositTransferMapRepository;
        this.depositTransferTransactionRepository = depositTransferTransactionRepository;
        this.transferMethodValidator = transferMethodValidator;
        this.transferOrchestratorService = transferOrchestratorService;
        this.responseCodeService = responseCodeService;
        this.depositTransferTransactionMapper = depositTransferTransactionMapper;
    }

    @Override
    public ProcessResult createSingle(Long id, TransferMethod transferMethod, String userId, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult processResult = new ProcessResult();

        try {
            // 1. Validasi parameter request.
            // id wajib ada karena digunakan untuk mengambil data DepositTransferMap.
            // transferMethod wajib ada karena akan dipakai saat transaksi dikirim ke middleware.
            validateCreateSingleRequest(id, transferMethod);

            // 2. Ambil data DepositTransferMap berdasarkan id.
            // DepositTransferMap adalah data detail hasil mapping dari SInvest.
            DepositTransferMap detail = depositTransferMapRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException(
                            "DepositTransferMap not found with id: " + id
                    ));

            // 3. Validasi data detail.
            // Hanya data dengan MappingStatus DRAFT dan belum memiliki transactionId
            // yang boleh dibuat menjadi transaction.
            validateSingleCandidate(detail);

            // 4. Validasi transfer method berdasarkan transfer scope.
            // INTERNAL hanya boleh OVERBOOKING.
            // EXTERNAL hanya boleh BIFAST, SKN, atau RTGS.
            transferMethodValidator.validate(
                    detail.getTransferScope(),
                    transferMethod
            );

            // 5. Build entity DepositTransferTransaction.
            // Untuk single, 1 data DepositTransferMap akan menjadi 1 transaction.
            // approvalStatus diset PENDING karena transaksi belum dikirim/diapprove.
            DepositTransferTransaction transaction = buildSingleTransaction(
                    detail,
                    transferMethod,
                    userId,
                    clientIp,
                    now
            );

            // 6. Simpan transaction header ke database.
            DepositTransferTransaction savedTransaction =
                    depositTransferTransactionRepository.save(transaction);

            // 7. Hubungkan data detail DepositTransferMap ke transaction yang baru dibuat.
            // transactionId dipakai sebagai penanda bahwa detail ini sudah masuk transaction.
            detail.setTransactionId(savedTransaction.getId());

            // 8. Untuk single, bulkReferenceId tidak dipakai.
            detail.setBulkReferenceId(null);

            // 9. Simpan perubahan DepositTransferMap.
            depositTransferMapRepository.save(detail);

            // 10. Tambahkan success ke ProcessResult.
            processResult.addSuccess();

        } catch (Exception e) {
            // 11. Jika proses gagal, catat log error dan tambahkan detail error ke ProcessResult.
            log.error("Failed to create single Deposit Transfer transaction. mapId={}, userId={}", id, userId, e);
            processResult.addError(
                    ErrorDetail.of(
                            ID,
                            id != null ? String.valueOf(id) : null,
                            Collections.singletonList(e.getMessage())
                    )
            );
        }

        return processResult;
    }

    @Override
    public ProcessResult createBulk(List<Long> ids, TransferMethod transferMethod, String userId, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult processResult = new ProcessResult();

        try {
            // 1. Validasi parameter request.
            // ids wajib berisi list id DepositTransferMap.
            // transferMethod wajib ada untuk menentukan metode transfer.
            validateCreateBulkRequest(ids, transferMethod);

            // 2. Ambil semua data DepositTransferMap berdasarkan list id.
            List<DepositTransferMap> details = depositTransferMapRepository.findAllById(ids);

            // 3. Pastikan semua id yang diminta ditemukan di database.
            // Jika jumlah data yang ditemukan berbeda, berarti ada id yang tidak valid.
            if (details.size() != ids.size()) {
                throw new IllegalArgumentException("Some DepositTransferMap data not found");
            }

            // 4. Validasi kandidat bulk.
            // Semua data harus DRAFT, belum punya transactionId,
            // dan beberapa field penting harus sama.
            validateBulkCandidate(details);

            // 5. Ambil data pertama sebagai baseline validasi transfer scope.
            DepositTransferMap reference = details.get(0);

            // 6. Validasi transfer method berdasarkan transfer scope.
            transferMethodValidator.validate(
                    reference.getTransferScope(),
                    transferMethod
            );

            // 7. Generate bulkReferenceId.
            // bulkReferenceId digunakan untuk menandai satu grup bulk.
            String bulkReferenceId = createBulkReferenceId();

            // 8. Build entity DepositTransferTransaction untuk BULK.
            // Banyak DepositTransferMap akan dijadikan 1 DepositTransferTransaction.
            DepositTransferTransaction transaction = buildBulkTransaction(
                    details,
                    transferMethod,
                    bulkReferenceId,
                    userId,
                    clientIp,
                    now
            );

            // 9. Simpan transaction header ke database.
            DepositTransferTransaction savedTransaction = depositTransferTransactionRepository.save(transaction);

            // 10. Hubungkan semua detail DepositTransferMap ke transaction yang sama.
            for (DepositTransferMap detail : details) {
                detail.setTransactionId(savedTransaction.getId());
                detail.setBulkReferenceId(bulkReferenceId);
                depositTransferMapRepository.save(detail);
            }

            // 11. Tambahkan success ke ProcessResult.
            processResult.addSuccess();

        } catch (Exception e) {
            // 12. Jika proses bulk gagal, catat log error dan masukkan error ke ProcessResult.
            log.error("Failed to create bulk Deposit Transfer transaction. ids={}, userId={}", ids, userId, e);
            processResult.addError(
                    ErrorDetail.of(
                            IDS,
                            ids != null ? String.valueOf(ids) : null,
                            Collections.singletonList(e.getMessage())
                    )
            );
        }

        return processResult;
    }

    @Override
    public ProcessResult send(List<Long> ids, String userId, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult processResult = new ProcessResult();

        // 1. Validasi list id transaksi.
        if (ids == null || ids.isEmpty()) {
            processResult.addError(
                    ErrorDetail.of(
                            IDS,
                            null,
                            Collections.singletonList("Transaction id list cannot be empty")
                    )
            );
            return processResult;
        }

        // 2. Ambil semua DepositTransferTransaction berdasarkan ids.
        List<DepositTransferTransaction> transactions = depositTransferTransactionRepository.findAllById(ids);

        // 3. Convert list menjadi map agar mudah mengambil transaksi sesuai urutan ids request.
        Map<Long, DepositTransferTransaction> transactionMap = new HashMap<>();

        for (DepositTransferTransaction transaction : transactions) {
            transactionMap.put(transaction.getId(), transaction);
        }

        // 4. Loop setiap id yang dikirim user.
        for (Long id : ids) {
            DepositTransferTransaction transaction = transactionMap.get(id);
            try {
                // 5. Validasi transaksi harus ditemukan.
                if (transaction == null) {
                    throw new DataNotFoundException(
                            "DepositTransferTransaction not found with id: " + id
                    );
                }

                // 6. Validasi transaksi boleh dikirim.
                // Hanya transactionStatus READY atau RETRY dan approvalStatus PENDING yang boleh dikirim.
                validateSendCandidate(transaction);

                // 7. Set status menjadi SENT sebelum call middleware.
                // Ini penting untuk audit bahwa transaksi sudah pernah dicoba dikirim.
                transaction.setTransactionStatus(TransactionStatus.SENT);
                transaction.setLastSentDate(now);

                // 8. Tambahkan retryCount.
                // Jika pertama kali kirim, retryCount menjadi 1.
                // Jika retry, retryCount bertambah.
                transaction.setRetryCount(
                        transaction.getRetryCount() == null
                                ? 1
                                : transaction.getRetryCount() + 1
                );

                // 9. Simpan status SENT sebelum hit middleware.
                depositTransferTransactionRepository.save(transaction);

                // 10. Build TransferableAdapter dari DepositTransferTransaction.
                // Adapter ini dipakai oleh TransferOrchestratorService untuk memilih executor.
                Transferable transferable = buildTransferableFromTransaction(transaction);

                // 11. Kirim transaksi ke middleware melalui orchestrator.
                // Orchestrator akan memilih executor berdasarkan transferMethod.
                TransferExecutionResult executionResult = transferOrchestratorService.execute(transferable);

                transaction.setInquiryReferenceId(executionResult.getInquiryReferenceId());

                // 12. Simpan referenceId dari middleware response.
                transaction.setReferenceId(executionResult.getReferenceId());

                // 13. Tentukan final transactionStatus berdasarkan responseCode.
                // SUCCESS jika response code sukses.
                // RETRY jika saldo kurang.
                // FAILED untuk error lain.
                applyResponseStatus(transaction, executionResult);

                // 14. Send transaction dianggap sebagai approval.
                // Maka approvalStatus berubah dari PENDING menjadi APPROVED.
                transaction.setApprovalStatus(ApprovalStatus.APPROVED);
                transaction.setApproveId(userId);
                transaction.setApproveDate(now);
                transaction.setApproveIpAddress(clientIp);

                // 15. Simpan hasil akhir transaksi.
                depositTransferTransactionRepository.save(transaction);

                // 16. Tambahkan success ke ProcessResult.
                processResult.addSuccess();

            } catch (Exception e) {
                // 17. Jika terjadi error saat send, catat log error.
                log.error(
                        "Failed to send Deposit Transfer transaction. transactionId={}, userId={}",
                        id,
                        userId,
                        e
                );

                // 18. Jika transaksi ditemukan, ubah status menjadi FAILED.
                if (transaction != null) {
                    transaction.setTransactionStatus(TransactionStatus.FAILED);
                    depositTransferTransactionRepository.save(transaction);
                }

                // 19. Tambahkan error ke ProcessResult.
                processResult.addError(
                        ErrorDetail.of(
                                ID,
                                String.valueOf(id),
                                Collections.singletonList(e.getMessage())
                        )
                );
            }
        }

        return processResult;
    }

    @Override
    public ProcessResult reject(List<Long> ids, String userId, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult processResult = new ProcessResult();

        // 1. Validasi list id transaction tidak boleh kosong.
        if (ids == null || ids.isEmpty()) {
            processResult.addError(
                    ErrorDetail.of(
                            IDS,
                            null,
                            Collections.singletonList("Transaction id list cannot be empty")
                    )
            );
            return processResult;
        }

        // 2. Ambil semua DepositTransferTransaction berdasarkan list id.
        List<DepositTransferTransaction> transactions = depositTransferTransactionRepository.findAllById(ids);

        // 3. Convert hasil query menjadi Map agar mudah cek transaction yang tidak ditemukan.
        Map<Long, DepositTransferTransaction> transactionMap = new HashMap<>();

        for (DepositTransferTransaction transaction : transactions) {
            transactionMap.put(transaction.getId(), transaction);
        }

        // 4. Loop berdasarkan ids dari request agar setiap id tetap diproses satu per satu.
        for (Long id : ids) {
            DepositTransferTransaction transaction = transactionMap.get(id);
            try {
                // 5. Validasi transaction harus ditemukan.
                if (transaction == null) {
                    throw new DataNotFoundException(
                            "DepositTransferTransaction not found with id: " + id
                    );
                }

                // 6. Validasi transaction boleh direject.
                // Hanya approvalStatus PENDING dan transactionStatus READY yang boleh direject.
                validateRejectCandidate(transaction);

                // 7. Set transaction menjadi REJECTED.
                // Record transaction tetap disimpan sebagai audit history.
                transaction.setApprovalStatus(ApprovalStatus.REJECTED);
                transaction.setApproveId(userId);
                transaction.setApproveDate(now);
                transaction.setApproveIpAddress(clientIp);

                depositTransferTransactionRepository.save(transaction);

                // 8. Ambil semua detail DepositTransferMap yang terhubung ke transaction ini.
                // SINGLE biasanya 1 detail, BULK bisa lebih dari 1 detail.
                List<DepositTransferMap> details = depositTransferMapRepository.findAllByTransactionId(transaction.getId());

                // 9. Lepaskan detail dari transaction agar bisa dibuat transaction ulang.
                for (DepositTransferMap detail : details) {
                    detail.setTransactionId(null);
                    detail.setBulkReferenceId(null);
                    detail.setMappingStatus(MappingStatus.DRAFT);
                    depositTransferMapRepository.save(detail);
                }

                // 10. Tambahkan success.
                processResult.addSuccess();

            } catch (Exception e) {
                log.error("Failed to reject Deposit Transfer transaction. transactionId={}, userId={}", id, userId, e);
                processResult.addError(
                        ErrorDetail.of(
                                ID,
                                String.valueOf(id),
                                Collections.singletonList(e.getMessage())
                        )
                );
            }
        }

        return processResult;
    }

    @Override
    public List<DepositTransferTransactionDto> getAllByCurrentDate(LocalDate currentDate) {
        if (currentDate == null) {
            throw new IllegalArgumentException("currentDate is required");
        }

        List<DepositTransferTransaction> entities =
                depositTransferTransactionRepository.findAllByDate(currentDate);

        return depositTransferTransactionMapper.fromEntitiesToDtos(entities);
    }

    @Override
    public List<DepositTransferTransactionDto> getAllByProcessTypeAndCurrentDate(ProcessType processType, LocalDate currentDate) {
        if (processType == null) {
            throw new IllegalArgumentException("processType is required");
        }

        if (currentDate == null) {
            throw new IllegalArgumentException("currentDate is required");
        }

        List<DepositTransferTransaction> entities =
                depositTransferTransactionRepository.findAllByProcessTypeAndDate(
                        processType,
                        currentDate
                );

        return depositTransferTransactionMapper.fromEntitiesToDtos(entities);
    }

    // =========================================================
    // BUILD TRANSACTION
    // =========================================================

    private DepositTransferTransaction buildSingleTransaction(
            DepositTransferMap detail,
            TransferMethod transferMethod,
            String userId,
            String clientIp,
            LocalDateTime now
    ) {
        return DepositTransferTransaction.builder()
                .imCode(detail.getImCode())
                .imName(detail.getImName())
                .fundCode(detail.getFundCode())
                .fundName(detail.getFundName())
                .bankCode(detail.getBankCode())
                .bankName(detail.getBankName())
                .cashAccountName(detail.getCashAccountName())
                .cashAccountNo(detail.getCashAccountNo())
                .currency(detail.getCurrency())
                .totalAmount(detail.getPrinciple())
                .date(detail.getDate())
                .siReferenceId(detail.getSiReferenceId())
                .referenceNo(detail.getReferenceNo())

                .accountDebitNo(detail.getAccountDebitNo())
                .productCode(detail.getProductCode())
                .biCode(detail.getBiCode())
                .bankType(detail.getBankType())
                .branchCode(detail.getBranchCode())

                .description(detail.getDescription())

                .bulkReferenceId(null)
                .bulkSiReferenceIds(null)

                .transferScope(detail.getTransferScope())
                .transferMethod(transferMethod)
                .processType(ProcessType.SINGLE)
                .transactionStatus(TransactionStatus.READY)

                .referenceId(null)
                .retryCount(0)
                .lastSentDate(null)

                .inputId(userId)
                .inputDate(now)
                .inputIpAddress(clientIp)
                .approvalStatus(ApprovalStatus.PENDING)
                .build();
    }

    private DepositTransferTransaction buildBulkTransaction(
            List<DepositTransferMap> details,
            TransferMethod transferMethod,
            String bulkReferenceId,
            String userId,
            String clientIp,
            LocalDateTime now
    ) {
        DepositTransferMap reference = details.get(0);

        String joinedSiReferenceIds = buildJoinedSiReferenceId(details);
        BigDecimal totalAmount = calculateTotalAmount(details);

        return DepositTransferTransaction.builder()
                .imCode(reference.getImCode())
                .imName(reference.getImName())
                .fundCode(reference.getFundCode())
                .fundName(reference.getFundName())
                .bankCode(reference.getBankCode())
                .bankName(reference.getBankName())
                .cashAccountName(reference.getCashAccountName())
                .cashAccountNo(reference.getCashAccountNo())
                .currency(reference.getCurrency())
                .totalAmount(totalAmount)
                .date(reference.getDate())

                .siReferenceId(null)
                .referenceNo(reference.getReferenceNo())

                .accountDebitNo(reference.getAccountDebitNo())
                .productCode(reference.getProductCode())
                .biCode(reference.getBiCode())
                .bankType(reference.getBankType())
                .branchCode(reference.getBranchCode())

                .description("Bulk Deposit Transfer - SI Reference: " + joinedSiReferenceIds)

                .bulkReferenceId(bulkReferenceId)
                .bulkSiReferenceIds(joinedSiReferenceIds)

                .transferScope(reference.getTransferScope())
                .transferMethod(transferMethod)
                .processType(ProcessType.BULK)
                .transactionStatus(TransactionStatus.READY)

                .referenceId(null)
                .retryCount(0)
                .lastSentDate(null)

                .inputId(userId)
                .inputDate(now)
                .inputIpAddress(clientIp)
                .approvalStatus(ApprovalStatus.PENDING)
                .build();
    }

    // =========================================================
    // BUILD TRANSFERABLE
    // =========================================================

    private Transferable buildTransferableFromTransaction(
            DepositTransferTransaction transaction
    ) {
        TransferableAdapter adapter = new TransferableAdapter();

        adapter.setId(transaction.getId());
        adapter.setDebitAccount(transaction.getAccountDebitNo());
        adapter.setCreditAccount(transaction.getCashAccountNo());
        adapter.setAmount(transaction.getTotalAmount());
        adapter.setDescription(transaction.getDescription());

        adapter.setTransferMethod(transaction.getTransferMethod());
        adapter.setTransferScope(transaction.getTransferScope());

        // adapter.setBeneficiaryBic(transaction.getBiCode());
        // adapter.setBeneficiaryName(transaction.getCashAccountName());
        // adapter.setBankCode(transaction.getBankCode());

        adapter.setProcessType(transaction.getProcessType());
        adapter.setBulkReferenceId(transaction.getBulkReferenceId());

        if (ProcessType.BULK.equals(transaction.getProcessType())) {
            adapter.setSiReferenceId(transaction.getBulkSiReferenceIds());
        } else {
            adapter.setSiReferenceId(transaction.getSiReferenceId());
        }

        return adapter;
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateCreateSingleRequest(Long id, TransferMethod transferMethod) {

        if (id == null) {
            throw new IllegalArgumentException("DepositTransferMap id is required");
        }

        if (transferMethod == null) {
            throw new IllegalArgumentException("Transfer method is required");
        }
    }

    private void validateCreateBulkRequest(List<Long> ids, TransferMethod transferMethod) {

        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("DepositTransferMap id list cannot be empty");
        }

        if (transferMethod == null) {
            throw new IllegalArgumentException("Transfer method is required");
        }
    }

    private static void validateSingleCandidate(DepositTransferMap data) {
        if (data == null) {
            throw new IllegalArgumentException("DepositTransferMap data is required");
        }

        if (!MappingStatus.DRAFT.equals(data.getMappingStatus())) {
            throw new IllegalStateException(
                    "Single transaction is rejected because only DRAFT record is allowed. " +
                            "id=" + data.getId() +
                            ", currentStatus=" + data.getMappingStatus()
            );
        }

        if (data.getTransactionId() != null) {
            throw new IllegalStateException(
                    "Single transaction is rejected because record already has transaction. " +
                            "id=" + data.getId() +
                            ", transactionId=" + data.getTransactionId()
            );
        }
    }

    private static void validateBulkCandidate(List<DepositTransferMap> candidateList) {

        if (candidateList == null || candidateList.isEmpty()) {
            throw new IllegalArgumentException("DepositTransferMap list must not be empty");
        }

        DepositTransferMap reference = candidateList.get(0);

        String referenceFundCode = reference.getFundCode();
        String referenceFundName = reference.getFundName();
        String referenceCashAccountNo = reference.getCashAccountNo();
        String referenceBankCode = reference.getBankCode();
        String referenceCurrency = reference.getCurrency();
        TransferScope referenceTransferScope = reference.getTransferScope();

        for (DepositTransferMap data : candidateList) {

            validateSingleCandidate(data);

            if (!Objects.equals(referenceFundCode, data.getFundCode())) {
                throw new IllegalStateException("Bulk operation is rejected because fundCode is inconsistent");
            }

            if (!Objects.equals(referenceFundName, data.getFundName())) {
                throw new IllegalStateException("Bulk operation is rejected because fundName is inconsistent");
            }

            if (!Objects.equals(referenceCashAccountNo, data.getCashAccountNo())) {
                throw new IllegalStateException("Bulk operation is rejected because cashAccountNo is inconsistent");
            }

            if (!Objects.equals(referenceBankCode, data.getBankCode())) {
                throw new IllegalStateException("Bulk operation is rejected because bankCode is inconsistent");
            }

            if (!Objects.equals(referenceCurrency, data.getCurrency())) {
                throw new IllegalStateException("Bulk operation is rejected because currency is inconsistent");
            }

            if (!Objects.equals(referenceTransferScope, data.getTransferScope())) {
                throw new IllegalStateException("Bulk operation is rejected because transferScope is inconsistent");
            }
        }
    }

    private static void validateSendStatus(TransactionStatus transactionStatus) {

        boolean canSend = TransactionStatus.READY.equals(transactionStatus)
                || TransactionStatus.RETRY.equals(transactionStatus);

        if (!canSend) {
            throw new IllegalStateException("Only READY or RETRY transaction can be sent");
        }
    }

    private static void validateSendCandidate(DepositTransferTransaction transaction) {
        validateSendStatus(transaction.getTransactionStatus());
        if (!ApprovalStatus.PENDING.equals(transaction.getApprovalStatus())) {
            throw new IllegalStateException(
                    "Only PENDING transaction can be sent. Current approvalStatus: "
                            + transaction.getApprovalStatus()
            );
        }
    }

    private static void validateRejectCandidate(DepositTransferTransaction transaction) {
        if (!ApprovalStatus.PENDING.equals(transaction.getApprovalStatus())) {
            throw new IllegalStateException(
                    "Only PENDING transaction can be rejected. Current approvalStatus: "
                            + transaction.getApprovalStatus()
            );
        }

        if (!TransactionStatus.READY.equals(transaction.getTransactionStatus())) {
            throw new IllegalStateException(
                    "Only READY transaction can be rejected. Current transactionStatus: "
                            + transaction.getTransactionStatus()
            );
        }
    }

    // =========================================================
    // RESPONSE HANDLER
    // =========================================================
    private void applyResponseStatus(
            DepositTransferTransaction transaction,
            TransferExecutionResult executionResult
    ) {
        if (executionResult== null) {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            return;
        }

        String responseCode = executionResult.getResponseCode();

        if (responseCodeService.isSuccess(responseCode)) {
            transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            return;
        }

        if (responseCodeService.isInsufficientBalance(responseCode)) {
            transaction.setTransactionStatus(TransactionStatus.RETRY);
            return;
        }

        transaction.setTransactionStatus(TransactionStatus.FAILED);
    }

    // =========================================================
    // COMMON HELPER
    // =========================================================

    private static String buildJoinedSiReferenceId(List<DepositTransferMap> list) {

        StringBuilder builder = new StringBuilder();

        if (list == null || list.isEmpty()) {
            return "";
        }

        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }

            builder.append(list.get(i).getSiReferenceId());
        }

        return builder.toString();
    }

    private static BigDecimal calculateTotalAmount(List<DepositTransferMap> list) {

        BigDecimal total = BigDecimal.ZERO;

        if (list == null || list.isEmpty()) {
            return total;
        }

        for (DepositTransferMap item : list) {
            if (item.getPrinciple() != null) {
                total = total.add(item.getPrinciple());
            }
        }

        return total;
    }

    private static String createBulkReferenceId() {

        String dateTime = ZonedDateTime.now(ZoneId.of("Asia/Jakarta"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        return dateTime + "_" + UUID.randomUUID()
                .toString()
                .replace("-", "");
    }

}
