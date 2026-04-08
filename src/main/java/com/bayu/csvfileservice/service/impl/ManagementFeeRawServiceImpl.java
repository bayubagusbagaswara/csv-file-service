package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ApproveDataChangeRequest;
import com.bayu.csvfileservice.dto.DeleteIdRequest;
import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.dto.managementfee.*;
import com.bayu.csvfileservice.exception.DataNotFoundException;
import com.bayu.csvfileservice.exception.InvalidFormatException;
import com.bayu.csvfileservice.mapper.DataChangeHelperMapper;
import com.bayu.csvfileservice.mapper.DataChangeMapper;
import com.bayu.csvfileservice.mapper.ManagementFeeMapper;
import com.bayu.csvfileservice.model.DataChange;
import com.bayu.csvfileservice.model.ManagementFeeRaw;
import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.model.enumerator.Month;
import com.bayu.csvfileservice.repository.ManagementFeeRawRepository;
import com.bayu.csvfileservice.service.DataChangeService;
import com.bayu.csvfileservice.service.ManagementFeeRawService;
import com.bayu.csvfileservice.util.JsonHelper;
import com.bayu.csvfileservice.util.ValidationData;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagementFeeRawServiceImpl implements ManagementFeeRawService {

    private static final String DATA_CHANGE_ID_FIELD = "dataChangeId";

    private final ManagementFeeRawRepository managementFeeRawRepository;
    private final ValidationData validationData;
    private final DataChangeMapper dataChangeMapper;
    private final DataChangeService dataChangeService;
    private final ManagementFeeMapper managementFeeMapper;
    private final DataChangeHelperMapper dataChangeHelperMapper;
    private final JsonHelper jsonHelper;

    @Override
    @Transactional
    public ProcessResult createBulk(ManagementFeeBulkRequest request, DataChangeDto dataChangeDto) {
        ProcessResult processResult = new ProcessResult();
        for (ManagementFeeRequest item : request.getItems()) {
            ErrorDetail errorDetail = processSingleRequest(item, dataChangeDto);
            if (errorDetail != null) {
                processResult.addError(errorDetail);
            } else {
                processResult.addSuccess();
            }
        }
        return processResult;
    }

    @Override
    @Transactional
    public ProcessResult createApprove(ApproveDataChangeRequest request, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult processResult = new ProcessResult();
        try {
            // Get Data Change - pastikan status PENDING
            DataChange dataChange = dataChangeService.getPendingById(request.getDataChangeId());

            // Parse JSON After - Untuk ADD operation, jsonAfter TIDAK mengandung id, month, year
            ManagementFeeDto afterPayload = jsonHelper.fromJson(dataChange.getJsonDataAfter(), ManagementFeeDto.class);

            // Extract month & year dari period (karena id, month, year tidak ada di jsonAfter)
            YearMonth ym = parsePeriod(afterPayload.getPeriod());
            Month month = Month.valueOf(ym.getMonth().name());
            Integer year = ym.getYear();

            // Check unique data
            if (managementFeeRawRepository.existsByFundCodeAndMonthAndYear(
                    afterPayload.getFundCode(), month, year)) {
                List<String> errors = new ArrayList<>();
                errors.add(String.format("Data already exists for the same fund %s, month %s, year %d",
                        afterPayload.getFundCode(), month.getLabel(), year));
                processResult.addError(
                        ErrorDetail.of(
                                DATA_CHANGE_ID_FIELD,
                                String.valueOf(dataChange.getId()),
                                errors
                        )
                );
                setApprovalFieldsToDataChange(dataChange, request, clientIp, null, now);
                dataChange.setDescription(String.format("DataChange recorded as REJECTED for duplicate fundCode %s, month %s, year %d",
                        afterPayload.getFundCode(), month.getLabel(), year));
                dataChangeService.setApprovalStatusIsRejected(dataChange, errors);
                return processResult;
            }

            // Build Entity
            ManagementFeeRaw managementFeeRaw = ManagementFeeRaw.builder()
                    .month(month)
                    .year(year)
                    .mutualFundName(afterPayload.getMutualFundName())
                    .investmentManager(afterPayload.getInvestmentManager())
                    .fundCode(afterPayload.getFundCode())
                    .debitAccount(afterPayload.getDebitAccount())
                    .amount(new BigDecimal(afterPayload.getAmount()))
                    .creditAccount(afterPayload.getCreditAccount())
                    .beneficiaryName(afterPayload.getBeneficiaryName())
                    .bankName(afterPayload.getBankName())
                    .paymentInstructions(afterPayload.getPaymentInstructions())
                    .paymentType(afterPayload.getPaymentType())
                    .period(afterPayload.getPeriod())
                    .description(afterPayload.getDescription())
                    .bankCode(afterPayload.getBankCode())
                    .referenceCombination(afterPayload.getReferenceCombination())
                    .build();

            // Set Approval fields ke entity
            setApprovalFields(managementFeeRaw, dataChange, request, clientIp, now);
            managementFeeRawRepository.save(managementFeeRaw);

            // Update Data Change dengan ID yang sudah dibuat
            setApprovalFieldsToDataChange(dataChange, request, clientIp, managementFeeRaw.getId(), now);

            // UPDATE: Simpan DTO lengkap (termasuk id, month, year) di jsonAfter setelah approve
            ManagementFeeDto completeDto = managementFeeMapper.toDto(managementFeeRaw);
            dataChange.setJsonDataAfter(jsonHelper.toJson(completeDto));

            dataChange.setDescription("Success approve insert of management fee with id: " + managementFeeRaw.getId());
            dataChangeService.setApprovalStatusIsApproved(dataChange);
            processResult.addSuccess();

        } catch (Exception e) {
            log.error("Error approve management fee for dataChangeId {}", request.getDataChangeId(), e);
            processResult.addError(
                    ErrorDetail.of(
                            DATA_CHANGE_ID_FIELD,
                            String.valueOf(request.getDataChangeId()),
                            List.of("Failed to approve data: " + e.getMessage())
                    )
            );
        }
        return processResult;
    }

    @Override
    public List<ManagementFeeRaw> getAll() {
        return managementFeeRawRepository.findAll();
    }

    @Override
    @Transactional
    public ProcessResult deleteById(DeleteIdRequest request, DataChangeDto dataChangeDto) {
        ProcessResult processResult = new ProcessResult();
        try {
            Long id = request.getId();

            // 1. Get entity
            ManagementFeeRaw entity = managementFeeRawRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException("ManagementFeeRaw not found with id: " + id));

            // 2. Map to ManagementFeeDto
            ManagementFeeDto dto = managementFeeMapper.fromEntityToDto(entity);

            // 3. Untuk DELETE: jsonBefore berisi id, month, year (menggunakan AuditForDelete)
            DataChangeDto dtoAudit = dataChangeHelperMapper.forDelete(dataChangeDto, dto);

            // 4. Map ke entity DataChange
            DataChange dataChange = dataChangeMapper.toEntity(dtoAudit);

            // 5. Simpan entityId yang akan dihapus
            dataChange.setEntityId(String.valueOf(entity.getId()));
            //todo: kenapa entityId disini nilainya string kosong ya jika sudah disimpan ke database? padahal log nya ada id nya
            log.info("id: {}", id);

            log.info("Datachange: {}", dataChange);

            // 6. Create delete action
            dataChangeService.createChangeActionDelete(dataChange, ManagementFeeRaw.class);

            processResult.addSuccess();

        } catch (Exception e) {
            log.error("Error deleteById id {}", request.getId(), e);
            processResult.addError(
                    ErrorDetail.of("id", String.valueOf(request.getId()), List.of(e.getMessage()))
            );
        }
        return processResult;
    }

    @Override
    @Transactional
    public ProcessResult deleteApprove(ApproveDataChangeRequest request, String clientIp) {
        ProcessResult processResult = new ProcessResult();
        LocalDateTime now = LocalDateTime.now();
        try {
            Long dataChangeId = request.getDataChangeId();

            // 1. Get DataChange + VALIDASI PENDING
            DataChange dataChange = dataChangeService.getPendingById(dataChangeId);

            log.info("datachange delete approve: {}", dataChange);

            Long entityId = dataChange.getEntityId() != null
                    ? Long.valueOf(dataChange.getEntityId())
                    : null;

            Optional<ManagementFeeRaw> optional = entityId != null
                    ? managementFeeRawRepository.findById(entityId)
                    : Optional.empty();

            if (optional.isPresent()) {
                ManagementFeeRaw entity = optional.get();

                // Set approval ke dataChange
                setApprovalFieldsToDataChange(dataChange, request, clientIp, entity.getId(), now);

                // Delete data
                managementFeeRawRepository.delete(entity);

                // Update audit json: jsonAfter tetap null untuk DELETE operation
                dataChange.setJsonDataAfter(null);
                dataChange.setDescription("Success delete management fee with id: " + entity.getId());
                dataChangeService.setApprovalStatusIsApproved(dataChange);
                processResult.addSuccess();

            } else {
                // Data not found
                setApprovalFieldsToDataChange(dataChange, request, clientIp, null, now);
                dataChangeService.setApprovalStatusIsRejected(
                        dataChange,
                        List.of("Management Fee not found")
                );
                processResult.addError(
                        ErrorDetail.of(
                                DATA_CHANGE_ID_FIELD,
                                String.valueOf(dataChangeId),
                                List.of("Management Fee not found")
                        )
                );
            }

        } catch (Exception e) {
            log.error("Error deleteApprove dataChangeId {}", request.getDataChangeId(), e);
            processResult.addError(
                    ErrorDetail.of(
                            "dataChangeId",
                            String.valueOf(request.getDataChangeId()),
                            List.of("Failed to approve delete: " + e.getMessage())
                    )
            );
        }
        return processResult;
    }

    // ======================== PROCESS =========================
    private ErrorDetail processSingleRequest(ManagementFeeRequest request, DataChangeDto dataChangeDto) {
        List<String> errors = new ArrayList<>();

        // Validation
        Set<ConstraintViolation<ManagementFeeRequest>> violations = validationData.validateObject(request);
        if (!violations.isEmpty()) {
            errors.addAll(
                    violations.stream()
                            .map(v -> v.getPropertyPath() + " : " + v.getMessage())
                            .toList()
            );
            log.warn("Validation failed for fundCode {}: {}", request.getFundCode(), errors);
            return buildError(request, errors);
        }

        // Check unique data
        YearMonth ym = parsePeriod(request.getPeriod());
        Month month = Month.valueOf(ym.getMonth().name());
        Integer year = ym.getYear();

        if (managementFeeRawRepository.existsByFundCodeAndMonthAndYear(request.getFundCode(), month, year)) {
            errors.add(String.format("Data already exists for the same fundCode %s, month %s, year %d",
                    request.getFundCode(), month.getLabel(), year));

            // Create dataChange with REJECTED status
            ManagementFeeDto feeDto = managementFeeMapper.fromRequestToDto(request);
            DataChangeDto dto = dataChangeHelperMapper.forAdd(dataChangeDto, feeDto);
            DataChange entity = dataChangeMapper.toEntity(dto);
            entity.setDescription(String.format("DataChange recorded as REJECTED for duplicate fundCode %s, month %s, year %d",
                    request.getFundCode(), month.getLabel(), year));
            dataChangeService.setApprovalStatusIsRejected(entity, errors);
            return buildError(request, errors);
        }

        // Business process
        try {
            ManagementFeeDto feeDto = managementFeeMapper.fromRequestToDto(request);

            // Untuk ADD: jsonAfter TIDAK mengandung id, month, year
            DataChangeDto dto = dataChangeHelperMapper.forAdd(dataChangeDto, feeDto);
            DataChange entity = dataChangeMapper.toEntity(dto);

            dataChangeService.createChangeActionAdd(entity, ManagementFeeRaw.class);

        } catch (Exception e) {
            log.error("Unexpected error for fundCode {}", request.getFundCode(), e);
            errors.add("Failed to process data: " + e.getMessage());
            return buildError(request, errors);
        }

        return null;
    }

    // ======================= HELPER ======================

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH);

    private YearMonth parsePeriod(String period) {
        try {
            return YearMonth.parse(period, PERIOD_FORMATTER);
        } catch (Exception e) {
            throw new InvalidFormatException("Invalid period format: " + period);
        }
    }

    private void setApprovalFieldsToDataChange(
            DataChange dataChange,
            ApproveDataChangeRequest request,
            String clientIp,
            Long id,
            LocalDateTime now
    ) {
        dataChange.setApproveId(request.getApproveId());
        dataChange.setApproveDate(now);
        dataChange.setApproveIpAddress(clientIp);
        dataChange.setEntityId(String.valueOf(id));
    }

    private void setApprovalFields(
            ManagementFeeRaw entity,
            DataChange dataChange,
            ApproveDataChangeRequest request,
            String clientIp,
            LocalDateTime now
    ) {
        entity.setApprovalStatus(ApprovalStatus.APPROVED);
        entity.setApproveId(request.getApproveId());
        entity.setApproveIpAddress(clientIp);
        entity.setApproveDate(now);
        entity.setInputId(dataChange.getInputId());
        entity.setInputIpAddress(dataChange.getInputIpAddress());
        entity.setInputDate(dataChange.getInputDate());
    }

    private ErrorDetail buildError(ManagementFeeRequest request, List<String> errors) {
        return ErrorDetail.builder()
                .field("fundCode")
                .value(request.getFundCode())
                .messages(errors.stream().distinct().toList())
                .build();
    }

}
