package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.dto.sinvest.SInvestBulkRequest;
import com.bayu.csvfileservice.dto.sinvest.SInvestDto;
import com.bayu.csvfileservice.dto.sinvest.SInvestRequest;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
            DataChange dataChange = dataChangeService.getPendingById(dataChangeId);

            SInvestDto sInvestDto = jsonHelper.fromJson(dataChange.getJsonDataAfter(), SInvestDto.class);

            siReferenceId = sInvestDto.getSiReferenceId();

            // ------------ Check unique data ------------


        } catch (Exception e) {

        }
        return processResult;
    }

    @Override
    public ProcessResult deleteById(Long id, DataChangeDto dataChangeDto) {
        return null;
    }

    @Override
    public ProcessResult approveDelete(Long dataChangeId, String userId, String clientIp) {
        return null;
    }

    @Override
    public SInvestDto getById(Long id) {
        return null;
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

        // --------------- Validation Data -------------------
        List<String> validationErrors = validateRequest(request);
        if (!validationErrors.isEmpty()) {
            errors.addAll(validationErrors);
            return ErrorDetailUtil.buildError(SI_REFERENCE_ID, siReferenceId, errors);
        }

        // --------------- Check unique data by SiReferenceId ------------
        if (sInvestRepository.existsBySiReferenceId(siReferenceId)) {
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
