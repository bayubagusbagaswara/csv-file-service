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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagementFeeRawServiceImpl implements ManagementFeeRawService {

    private final ManagementFeeRawRepository managementFeeRawRepository;
    private final ValidationData validationData;
    private final DataChangeMapper dataChangeMapper;
    private final DataChangeService dataChangeService;
    private final ManagementFeeMapper managementFeeMapper;
    private final DataChangeHelperMapper dataChangeHelperMapper;
    private final JsonHelper jsonHelper;

    @Override
    public ProcessResult createBulk(ManagementFeeBulkRequest request, DataChangeDto dataChangeDto) {
        ProcessResult processResult = new ProcessResult();
        for (ManagementFeeRequest item : request.getItems()) {
            ErrorDetail errorDetail = processSingleRequest(item, dataChangeDto);
            if (errorDetail != null) {
                processResult.addError(errorDetail); // didalam method addError sudah ada totalFailed++
            } else {
                processResult.addSuccess();
            }
        }
        return processResult;
    }

    @Override
    public ProcessResult createApprove(ApproveDataChangeRequest request, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult processResult = new ProcessResult();
        try {

            // Get Data Change
            DataChange dataChange = dataChangeService.getById(request.getDataChangeId());

            // Parse JSON
            ManagementFeeDto afterPayload = jsonHelper.fromJson(dataChange.getJsonDataAfter(), ManagementFeeDto.class);

            // Parse Period
            YearMonth ym = parsePeriod(afterPayload.getPeriod());
            Month month = Month.valueOf(ym.getMonth().name());
            Integer year = ym.getYear();


            // Delete Existing
            managementFeeRawRepository.deleteByFundCodeAndMonthAndYear(
                    afterPayload.getFundCode(),
                    month,
                    year
            );

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
                    .build();

            // Set Approval
            setApprovalFields(managementFeeRaw, dataChange, request, clientIp, now);
            managementFeeRawRepository.save(managementFeeRaw);

            // Update Data Change
            setApprovalFieldsToDataChange(dataChange, request, clientIp, managementFeeRaw.getId(), now);

            dataChange.setJsonDataAfter(jsonHelper.toJson(managementFeeMapper.toDto(managementFeeRaw)));

            dataChange.setDescription("Success approve insert of management fee with id: " + managementFeeRaw.getId());
            dataChangeService.setApprovalStatusIsApproved(dataChange);
            processResult.addSuccess();
        } catch (Exception e) {
            log.error("Error approve management fee for dataChangeId {}", request.getDataChangeId(), e);
            processResult.addError(
                    ErrorDetail.of(
                            "dataChangeId",
                            String.valueOf(request.getDataChangeId()),
                            List.of("Failed to approve data")
                    )
            );
        }
        return processResult;
    }

    public ProcessResult deleteById(DeleteIdRequest request, DataChangeDto dataChangeDto) {

        ProcessResult processResult = new ProcessResult();

        try {
            Long id = request.getId();

            //1. get entity
            ManagementFeeRaw entity = managementFeeRawRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException("ManagementFeeRaw nor found with id: " + id));

            //2. map to ManagementFeeDto
            ManagementFeeDto dto = managementFeeMapper.toDto(entity);

            //3. map from dto to ViewManagementFeeAfterDelete
            ViewManagementFeeBeforeDelete view = managementFeeMapper.fromRequestToViewBeforeDelete(dto);

            //4. map dataChangeDto to data change
            DataChangeDto dtoAudit = dataChangeHelperMapper.forDelete(dataChangeDto, view);

            //5. map ke entity DataChange
            DataChange dataChange = dataChangeMapper.toEntity(dtoAudit);

            //6. create delete action
            dataChangeService.createChangeActionDelete(
                    dataChange,
                    ManagementFeeRaw.class
            );

            processResult.addSuccess();
        } catch (Exception e) {
            log.error("Error deleteById id {}", request.getId(), e);
            processResult.addError(
                    ErrorDetail.of("id", String.valueOf(request.getId()), List.of(e.getMessage()))
            );
        }
        return processResult;
    }

    public ProcessResult deleteApprove(ApproveDataChangeRequest request, String clientIp) {
        ProcessResult processResult = new ProcessResult();
        LocalDateTime now = LocalDateTime.now();
        try {
            Long dataChangeId = request.getDataChangeId();

            // 1. get DataChange + VALIDASI PENDING
            DataChange dataChange = dataChangeService.getPendingById(dataChangeId);

            Long entityId = dataChange.getEntityId() != null
                    ? Long.valueOf(dataChange.getEntityId())
                    : null;

            Optional<ManagementFeeRaw> optional = entityId != null
                    ? managementFeeRawRepository.findById(entityId)
                    : Optional.empty();

            // ================= SUCCESS =================
            if (optional.isPresent()) {
                ManagementFeeRaw entity = optional.get();

                // set approval ke dataChange
                setApprovalFieldsToDataChange(
                        dataChange,
                        request,
                        clientIp,
                        entity.getId(),
                        now
                );

                // delete data
                managementFeeRawRepository.delete(entity);

                // update audit json
                // todo: setJsonDataBefore tidak perlu ya? karena pada saat pertama sudah di-insert?
                dataChange.setJsonDataAfter(null);
                dataChange.setDescription(
                        "Success dalete management fee with id: " + entity.getId());
                dataChangeService.setApprovalStatusIsApproved(dataChange);
                processResult.addSuccess();
            } else {
                // ================= NOT FOUND =================
                setApprovalFieldsToDataChange(
                        dataChange,
                        request,
                        clientIp,
                        null,
                        now
                );
                dataChangeService.setApprovalStatusIsRejected(
                        dataChange,
                        List.of("Management Fee not found")
                );
                processResult.addError(
                        ErrorDetail.of(
                                "dataChangeId",
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
                            List.of("Failed to approve delete")
                    )
            );
        }
        return processResult;
    }

    // ======================== PROCESS =========================
    private ErrorDetail processSingleRequest(
            ManagementFeeRequest request,
            DataChangeDto dataChangeDto
    ) {

        List<String> errors = new ArrayList<>();

        // ================= VALIDATION =================
        Set<ConstraintViolation<ManagementFeeRequest>> violations =
                validationData.validateObject(request);

        if (!violations.isEmpty()) {

            errors.addAll(
                    violations.stream()
                            .map(v -> v.getPropertyPath() + " : " + v.getMessage())
                            .toList()
            );

            log.warn("Validation failed for fundCode {}: {}", request.getFundCode(), errors);

            return buildError(request, errors);
        }

        // ================= BUSINESS PROCESS =================
        try {

            //todo: berarti tidak perlu ViewManagementFeeAfterAdd ya? bisa langsung saja ke ManagementFeeDto?
            ViewManagementFeeAfterAdd view = managementFeeMapper.fromRequestToViewAfterAdd(request);

            DataChangeDto dto = dataChangeHelperMapper.forAdd(dataChangeDto, view);

            DataChange entity = dataChangeMapper.toEntity(dto);

            dataChangeService.createChangeActionAdd(entity, ManagementFeeRaw.class);

        } catch (Exception e) {
            log.error("Unexpected error for fundCode {}", request.getFundCode(), e);
            errors.add("Failed to process data");
            return buildError(request, errors);
        }

        return null; // success
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
