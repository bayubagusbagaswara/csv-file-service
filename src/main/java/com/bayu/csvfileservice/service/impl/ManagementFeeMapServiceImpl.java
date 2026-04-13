package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.managementfee.CreateTransactionRequest;
import com.bayu.csvfileservice.exception.DataNotFoundException;
import com.bayu.csvfileservice.model.*;
import com.bayu.csvfileservice.model.enumerator.*;
import com.bayu.csvfileservice.repository.*;
import com.bayu.csvfileservice.service.ManagementFeeMapService;
import com.bayu.csvfileservice.service.ResponseCodeService;
import com.bayu.csvfileservice.util.BankCodeHelper;
import com.bayu.csvfileservice.util.TransferMethodValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final ResponseCodeService responseCodeService;

    @Override
    @Transactional
    public ProcessResult map(Month month, Integer year) {

        ProcessResult result = new ProcessResult();

        // Replace strategy
        //todo: apakah harusnya ini ada tambahan kondisi ya? karena data Map bisa jadi emang sudah pernah dijalankan (kirim ke Middleware)
        mapRepository.deleteByMonthAndYear(month, year);

        List<ManagementFeeRaw> feeRawList = rawRepository.findAllByMonthAndYear(month, year);

        if (feeRawList.isEmpty()) {
            log.warn("No raw data found for {} {}", month, year);
            return result;
        }

        for (ManagementFeeRaw raw : feeRawList) {
            try {
                validateRaw(raw);

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
    public ProcessResult createTransactions(List<CreateTransactionRequest> requests) {
        ProcessResult result = new ProcessResult();

        for (CreateTransactionRequest req : requests) {

            try {

                ManagementFeeMap item = mapRepository.findById(req.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Data not found"));

                if (item.getStatus() != MappingStatus.DRAFT) {
                    throw new IllegalStateException("Only DRAFT data can be processed");
                }

                // VALIDASI METHOD
                validator.validate(
                        item.getTransferScope(),
                        req.getTransferMethod()
                );

                item.setTransferMethod(req.getTransferMethod());
                item.setStatus(MappingStatus.READY);

                mapRepository.save(item);

                result.addSuccess();

            } catch (Exception e) {

                result.addError(
                        ErrorDetail.of(
                                "id",
                                String.valueOf(req.getId()),
                                List.of(e.getMessage())
                        )
                );
            }
        }

        return result;

    }

    @Override
    public ProcessResult sendTransactions(List<Long> ids) {
        ProcessResult result = new ProcessResult();
        List<ManagementFeeMap> list = mapRepository.findAllById(ids);

        for (ManagementFeeMap item : list) {
            try {
                validateSend(item);

                // Execute transfer
                NcbsResponse response = routeTransferExecution(item);

                // ================= UPDATE MAP =================
                item.setStatus(MappingStatus.SENT);
                item.setReferenceId(response.getReferenceId());
                item.setLastSentDate(LocalDateTime.now());
                item.setRetryCount(
                        item.getRetryCount() == null ? 1 : item.getRetryCount() + 1
                );

                mapRepository.save(item);

                // ================= UPDATE STATUS =================
                updateStatus(item, response);
                result.addSuccess();

            } catch (Exception e) {
                log.error("Send failed id {}", item.getId(), e);
                item.setStatus(MappingStatus.FAILED);
                mapRepository.save(item);
                result.addError(
                        ErrorDetail.of(
                                "id",
                                String.valueOf(item.getId()),
                                List.of("Send failed")
                        )
                );
            }
        }
        return result;
    }

    // ======================= ROUTER ======================
    private NcbsResponse routeTransferExecution(ManagementFeeMap item) {
        return switch (item.getTransferScope()) {
            case INTERNAL -> executeInternalTransfer(item);
            case EXTERNAL -> executeExternalTransfer(item);
        };
    }

    // ======================= INTERNAL =====================
    private NcbsResponse executeInternalTransfer(ManagementFeeMap item) {
        if (item.getTransferMethod() != TransferMethod.OVERBOOKING) {
            throw new IllegalArgumentException("Internal transfer must use OVERBOOKING");
        }
        return executeOverbookingTransferr(item);
    }

    // ======================= EXTERNAL =====================
    private NcbsResponse executeExternalTransfer(ManagementFeeMap item) {
        return switch (item.getTransferMethod()) {
            case BI_FAST -> executeBiFastTransfer(item);
            case SKN, RTGS -> executeSknRtgsTransfer(item);
            case OVERBOOKING ->
                throw new IllegalArgumentException("External transfer cannot use OVERBOOKING");
            default -> throw new IllegalStateException("Unsupported transfer method");
        };
    }

    // ======================= OVERBOOKING ========================
    private NcbsResponse executeOverbookingTransfer(ManagementFeeMap item) {
        NcbsRequest request = buildNcbsRequestPayload(
                item, MiddlewareServiceType.OVERBOOKING_CASA
        );
        ncbsRequestRepository.save(request);
        NcbsResponse response = invokeOverbookingMiddleware(request);
        ncbsResponseRepository.save(response);
        return response;
    }

    // ======================= BI-FAST =======================

    private NcbsResponse executeBiFastTransfer(ManagementFeeMap item) {

        NcbsRequest request = buildNcbsRequestPayload(
                item,
                MiddlewareServiceType.TRANSFER_SKN_RTGS
        );

        requestRepository.save(request);

        NcbsResponse response = invokeBiFastMiddleware(request);

        responseRepository.save(response);

        return response;
    }

    // ======================= SKN / RTGS =======================

    private NcbsResponse executeSknRtgsTransfer(ManagementFeeMap item) {

        NcbsRequest request = buildNcbsRequestPayload(
                item,
                MiddlewareServiceType.TRANSFER_SKN_RTGS
        );

        requestRepository.save(request);

        NcbsResponse response = invokeSknRtgsMiddleware(request);

        responseRepository.save(response);

        return response;
    }

    // ======================= BUILD REQUEST =======================

    private NcbsRequest buildNcbsRequestPayload(
            ManagementFeeMap item,
            MiddlewareServiceType service
    ) {

        return NcbsRequest.builder()
                .referenceId(UUID.randomUUID().toString())
                .entityId(item.getId())
                .createdDate(LocalDateTime.now())
                .transferScope(item.getTransferScope())
                .transferMethod(item.getTransferMethod())
                .service(service)
                .build();
    }

    // ======================= MIDDLEWARE =======================

    private NcbsResponse invokeOverbookingMiddleware(NcbsRequest request) {
        return middlewareService.callOverbooking(request);
    }

    private NcbsResponse invokeBiFastMiddleware(NcbsRequest request) {
        return middlewareService.callBiFast(request);
    }

    private NcbsResponse invokeSknRtgsMiddleware(NcbsRequest request) {
        return middlewareService.callSknRtgs(request);
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


    private void validateSend(ManagementFeeMap item) {
        if (!List.of(MappingStatus.READY, MappingStatus.RETRY).contains(item.getStatus())) {
            throw new IllegalStateException("Data with id " + item.getId() + " cannot be sent");
        }
    }

    private MiddlewareServiceType resolveService(ManagementFeeMap item) {

        return item.getTransferMethod() == TransferMethod.OVERBOOKING
                ? MiddlewareServiceType.OVERBOOKING_CASA
                : MiddlewareServiceType.TRANSFER_SKN_RTGS;
    }

    private NcbsResponse simulateResponse(NcbsRequest request) {

        // TODO: replace with real integration
        return NcbsResponse.builder()
                .referenceId(request.getReferenceId())
                .createdDate(LocalDateTime.now())
                .ncbsStatus(NcbsStatus.SUCCESS)
                .build();
    }

    private void updateStatus(ManagementFeeMap item, NcbsResponse response) {
        if (response.getNcbsStatus() == NcbsStatus.SUCCESS) {
            item.setStatus(MappingStatus.SUCCESS);
        } else if (responseCodeService.isInsufficientBalance(response.getResponseCode())) {
            item.setStatus(MappingStatus.RETRY);
        } else {
            item.setStatus(MappingStatus.FAILED);
        }
        mapRepository.save(item);
    }

}
