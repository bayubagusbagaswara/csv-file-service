package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.dto.sinvest.SInvestBulkRequest;
import com.bayu.csvfileservice.dto.sinvest.SInvestDto;
import com.bayu.csvfileservice.dto.sinvest.SInvestRequest;
import com.bayu.csvfileservice.exception.DataNotFoundException;
import com.bayu.csvfileservice.mapper.DataChangeHelperMapper;
import com.bayu.csvfileservice.mapper.DataChangeMapper;
import com.bayu.csvfileservice.mapper.SInvestMapper;
import com.bayu.csvfileservice.model.DataChange;
import com.bayu.csvfileservice.model.SInvest;
import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.repository.DepositTransferMapRepository;
import com.bayu.csvfileservice.repository.SInvestRepository;
import com.bayu.csvfileservice.service.DataChangeService;
import com.bayu.csvfileservice.service.SInvestService;
import com.bayu.csvfileservice.util.*;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.bayu.csvfileservice.util.RequestDataUtil.trimIfNotNull;

@Service
@Slf4j
public class SInvestServiceImpl implements SInvestService {

    private static final String SI_REFERENCE_ID = "siReferenceId";
    private static final String NOT_FOUND = "SInvest not found with id: ";

    private final SInvestRepository sInvestRepository;
    private final SInvestMapper sInvestMapper;
    private final ValidationData validationData;
    private final DataChangeService dataChangeService;
    private final DataChangeMapper dataChangeMapper;
    private final DataChangeHelperMapper dataChangeHelperMapper;
    private final JsonHelper jsonHelper;
    private final DepositTransferMapRepository depositTransferMapRepository;

    public SInvestServiceImpl(SInvestRepository sInvestRepository, SInvestMapper sInvestMapper, ValidationData validationData, DataChangeService dataChangeService, DataChangeMapper dataChangeMapper, DataChangeHelperMapper dataChangeHelperMapper, JsonHelper jsonHelper, DepositTransferMapRepository depositTransferMapRepository) {
        this.sInvestRepository = sInvestRepository;
        this.sInvestMapper = sInvestMapper;
        this.validationData = validationData;
        this.dataChangeService = dataChangeService;
        this.dataChangeMapper = dataChangeMapper;
        this.dataChangeHelperMapper = dataChangeHelperMapper;
        this.jsonHelper = jsonHelper;
        this.depositTransferMapRepository = depositTransferMapRepository;
    }

    @Override
    public ProcessResult createBulk(SInvestBulkRequest request, DataChangeDto dataChangeDto) {
        ProcessResult result = new ProcessResult();

        for (SInvestRequest item : request.getItems()) {
            ErrorDetail errorDetail = processSingleRequest(item, dataChangeDto);
            if (errorDetail != null) {
                result.addError(errorDetail);
            } else {
                result.addSuccess();
            }
        }
        return result;
    }

    @Override
    public ProcessResult approveCreate(Long dataChangeId, String userId, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult processResult = new ProcessResult();
        String siReferenceId = "";

        try {
            List<String> errors = new ArrayList<>();

            DataChange dataChange = dataChangeService.getPendingById(dataChangeId);

            SInvestDto sInvestDto = jsonHelper.fromJson(dataChange.getJsonDataAfter(), SInvestDto.class);

            siReferenceId = sInvestDto.getSiReferenceId();

            LocalDate date = parseToLocalDate(sInvestDto.getDate());

            // ------------ Check unique data ------------
            if (sInvestRepository.existsBySiReferenceIdAndDate(siReferenceId, date)) {
                // ------------ set approval field to data change -----------
                setApprovalFieldsToDataChange(dataChange, userId, clientIp, null, now);
                dataChange.setDescription(
                        String.format("Rejected because S-Invest data with siReferenceId '%s' already exists", sInvestDto.getSiReferenceId())
                );

                // ------------ add error ----------------
                errors.add(String.format("S-Invest data with siReferenceId '%s' already exists", sInvestDto.getSiReferenceId()));

                // ------------ set approval status is rejected ------------
                dataChangeService.setApprovalStatusIsRejected(dataChange, errors);

                processResult.addError(ErrorDetailUtil.buildError(SI_REFERENCE_ID, siReferenceId, errors));

                return processResult;
            }

            // ------------ Build Entity ---------------
            SInvest sInvest = SInvest.builder()
                    .imCode(sInvestDto.getImCode())
                    .imName(sInvestDto.getImName())
                    .fundCode(sInvestDto.getFundCode())
                    .fundName(sInvestDto.getFundName())
                    .bankCode(sInvestDto.getBankCode())
                    .bankName(sInvestDto.getBankName())
                    .cashAccountName(sInvestDto.getCashAccountName())
                    .cashAccountNo(sInvestDto.getCashAccountNo())
                    .currency(sInvestDto.getCurrency())
                    .principle(new BigDecimal(sInvestDto.getPrinciple()))
                    .date(parseToLocalDate(sInvestDto.getDate()))
                    .siReferenceId(sInvestDto.getSiReferenceId())
                    .referenceNo(sInvestDto.getReferenceNo())
                    .build();

            // ------------ set approval field ---------------
            setApprovalFields(sInvest, dataChange, userId, clientIp, now);

            // ------------ save entity ------------------
            sInvestRepository.save(sInvest);

            // ------------ set approval fields to data change -----------------
            setApprovalFieldsToDataChange(dataChange, userId, clientIp, sInvest.getId(), now);

            // ------------ set json data after -----------------
            SInvestDto completeDto = sInvestMapper.fromEntityToDto(sInvest);
            dataChange.setJsonDataAfter(jsonHelper.toJson(completeDto));

            // ------------ set approval status is approved ------------
            dataChange.setDescription("Success approve insert of SInvest with id: " + sInvest.getId());
            dataChangeService.setApprovalStatusIsApproved(dataChange);

            // ------------ success ---------------------
            processResult.addSuccess();

        } catch (Exception e) {
            log.error("Error approve SInvest for dataChangeId={} and siReferenceId={}", dataChangeId, siReferenceId, e);
            processResult.addError(
                    ErrorDetail.of(
                            SI_REFERENCE_ID, siReferenceId,
                            Collections.singletonList("Failed to approve S-Invest data " + e.getMessage())
                    )
            );
        }

        return processResult;
    }

    @Override
    public ProcessResult deleteById(Long id, DataChangeDto dataChangeDto) {
        ProcessResult processResult = new ProcessResult();
        String siReferenceId = "";

        try {

            List<String> errors = new ArrayList<>();

            SInvest sInvest = sInvestRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException(NOT_FOUND + id));

            // ----------- get siReferenceId -------------
            siReferenceId = sInvest.getSiReferenceId();

            // ----------- get date --------------
            LocalDate date = sInvest.getDate();

            // ----------- check data in the Map table ----------------
            if (depositTransferMapRepository.existsBySiReferenceIdAndDate(siReferenceId, date)) {
                errors.add(String.format("SInvest data has been mapped with siReferenceId '%s'", siReferenceId));

                processResult.addError(ErrorDetailUtil.buildError(SI_REFERENCE_ID, siReferenceId, errors));

                return processResult;
            }

            // --------- map from entity to dto ----------
            SInvestDto sInvestDto = sInvestMapper.fromEntityToDto(sInvest);

            DataChangeDto dtoAudit = dataChangeHelperMapper.forDelete(dataChangeDto, sInvestDto);

            DataChange dataChange = dataChangeMapper.toEntity(dtoAudit);

            dataChange.setEntityId(String.valueOf(sInvest.getId()));

            dataChangeService.createChangeActionDelete(dataChange, SInvest.class);

            processResult.addSuccess();

        } catch (Exception e) {
            log.error("Error delete by id {} and siReferenceId {}", id, siReferenceId, e);
            processResult.addError(
                    ErrorDetail.of(
                            SI_REFERENCE_ID, siReferenceId,
                            Collections.singletonList("Failed delete by id: " + e.getMessage())
                    )
            );
        }
        return processResult;
    }

    @Override
    public ProcessResult approveDelete(Long dataChangeId, String userId, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult processResult = new ProcessResult();
        String siReferenceId = "";

        try {

            DataChange dataChange = dataChangeService.getPendingById(dataChangeId);

            Long entityId = dataChange.getEntityId() != null
                    ? Long.valueOf(dataChange.getEntityId())
                    : null;

            Optional<SInvest> optional = entityId != null
                    ? sInvestRepository.findById(entityId)
                    : Optional.empty();

            if (optional.isPresent()) {
                SInvest sInvest = optional.get();
                siReferenceId = sInvest.getSiReferenceId();

                setApprovalFieldsToDataChange(dataChange, userId, clientIp, sInvest.getId(), now);

                // delete
                sInvestRepository.delete(sInvest);

                dataChange.setJsonDataAfter(null);
                dataChange.setDescription("Success delete SInvest with id: " + sInvest.getId());
                dataChangeService.setApprovalStatusIsApproved(dataChange);
                processResult.addSuccess();

            } else {
                setApprovalFieldsToDataChange(dataChange, userId, clientIp, null, now);
                dataChangeService.setApprovalStatusIsRejected(
                        dataChange,
                        Collections.singletonList(NOT_FOUND + dataChange.getEntityId())
                );

                processResult.addError(
                        ErrorDetail.of(
                                SI_REFERENCE_ID,
                                siReferenceId,
                                Collections.singletonList(NOT_FOUND + entityId)
                        )
                );
            }
        } catch (Exception e) {
            log.error("Error delete approve with dataChangeId={}, siReferenceId={}", dataChangeId, siReferenceId, e);
            processResult.addError(
                    ErrorDetail.of(
                            SI_REFERENCE_ID,
                            siReferenceId,
                            Collections.singletonList("Failed to approve delete: " + e.getMessage())
                    )
            );
        }
        return processResult;
    }

    @Override
    public SInvestDto getById(Long id) {
        SInvest sInvest = sInvestRepository.findById(id).orElseThrow(() -> new DataNotFoundException(NOT_FOUND + id));
        return sInvestMapper.fromEntityToDto(sInvest);
    }

    @Override
    public Boolean isDeletableBySiReferenceId(String siReferenceId) {
        return null;
    }

    // ========================== PROCESS BUSINESS =================================
    private ErrorDetail processSingleRequest(
            SInvestRequest request,
            DataChangeDto dataChangeDto
    ) {
        List<String> errors = new ArrayList<>();
        trimRequestData(request);
        String siReferenceId = request.getSiReferenceId();
        LocalDate date = parseToLocalDate(request.getDate());

        // --------------- Validation Data -------------------
        List<String> validationErrors = validateRequest(request);
        if (!validationErrors.isEmpty()) {
            errors.addAll(validationErrors);
            return ErrorDetailUtil.buildError(SI_REFERENCE_ID, siReferenceId, errors);
        }

        // --------------- Check unique data by SiReferenceId ------------
        if (sInvestRepository.existsBySiReferenceIdAndDate(siReferenceId, date)) {
            errors.add(String.format("Data already exists for the same SiReferenceId '%s'", siReferenceId));
            return ErrorDetailUtil.buildError(SI_REFERENCE_ID, siReferenceId, errors);
        }

        // ---------------- Business Process --------------------------
        try {
            SInvestDto sInvestDto = sInvestMapper.fromRequestToDto(request);

            DataChangeDto dto = dataChangeHelperMapper.forAdd(dataChangeDto, sInvestDto);

            DataChange dataChange = dataChangeMapper.toEntity(dto);

            dataChangeService.createChangeActionAdd(dataChange, SInvest.class);

        } catch (Exception e) {
            log.error("Unexpected error for siReferenceId {}", siReferenceId, e);
            errors.add("Failed to process data: " + e.getMessage());
            return ErrorDetailUtil.buildError(SI_REFERENCE_ID, siReferenceId, errors);
        }
        return null;
    }

    // ========================= HELPER ==============================================
    private static LocalDate parseToLocalDate(String date) {
        try {
            return DateUtil.parseToLocalDate(date, DateFormatConstant.YYYYMMDD);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid date format. Expected format is yyyyMMdd. Value " + date
            );
        }
    }

    private static void trimRequestData(SInvestRequest request) {
        trimIfNotNull(request::getImCode, request::setImCode);
        trimIfNotNull(request::getImName, request::setImName);
        trimIfNotNull(request::getFundCode, request::setFundCode);
        trimIfNotNull(request::getFundName, request::setFundName);
        trimIfNotNull(request::getBankCode, request::setBankCode);
        trimIfNotNull(request::getBankName, request::setBankName);
        trimIfNotNull(request::getCashAccountName, request::setCashAccountName);
        trimIfNotNull(request::getCashAccountNo, request::setCashAccountNo);
        trimIfNotNull(request::getCurrency, request::setCurrency);
        trimIfNotNull(request::getPrinciple, request::setPrinciple);
        trimIfNotNull(request::getDate, request::setDate);
        trimIfNotNull(request::getReferenceNo, request::setReferenceNo);
        trimIfNotNull(request::getSiReferenceId, request::setSiReferenceId);
    }

    private List<String> validateRequest(SInvestRequest request) {
        List<String> validationErrors = new ArrayList<>();
        Set<ConstraintViolation<SInvestRequest>> constraintViolations = validationData.validateObject(request);
        if (!constraintViolations.isEmpty()) {
            validationErrors.addAll(constraintViolations.stream()
                    .map(v -> v.getPropertyPath() + " : " + v.getMessage())
                    .toList());
        }
        return validationErrors;
    }

    private void setApprovalFields(SInvest sInvest, DataChange dataChange, String userId, String clientIp, LocalDateTime now) {
        sInvest.setApprovalStatus(ApprovalStatus.APPROVED);
        sInvest.setApproveId(userId);
        sInvest.setApproveIpAddress(clientIp);
        sInvest.setApproveDate(now);
        sInvest.setInputId(dataChange.getInputId());
        sInvest.setInputIpAddress(dataChange.getInputIpAddress());
        sInvest.setInputDate(dataChange.getInputDate());
    }

    private void setApprovalFieldsToDataChange(DataChange dataChange, String userId, String clientIp, Long id, LocalDateTime now) {
        dataChange.setApproveId(userId);
        dataChange.setApproveIpAddress(clientIp);
        dataChange.setApproveDate(now);
        dataChange.setEntityId(String.valueOf(id));
    }

}
