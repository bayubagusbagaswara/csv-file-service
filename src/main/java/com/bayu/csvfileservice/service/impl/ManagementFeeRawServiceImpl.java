package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ApproveDataChangeRequest;
import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.dto.managementfee.ManagementFeeBulkRequest;
import com.bayu.csvfileservice.dto.managementfee.ManagementFeeRequest;
import com.bayu.csvfileservice.dto.managementfee.ViewManagementFeeAfterAdd;
import com.bayu.csvfileservice.mapper.DataChangeHelperMapper;
import com.bayu.csvfileservice.mapper.DataChangeMapper;
import com.bayu.csvfileservice.mapper.ManagementFeeMapper;
import com.bayu.csvfileservice.model.DataChange;
import com.bayu.csvfileservice.model.ManagementFeeRaw;
import com.bayu.csvfileservice.service.DataChangeService;
import com.bayu.csvfileservice.service.ManagementFeeRawService;
import com.bayu.csvfileservice.util.ValidationData;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagementFeeRawServiceImpl implements ManagementFeeRawService {

    private final ValidationData validationData;
    private final DataChangeMapper dataChangeMapper;
    private final DataChangeService dataChangeService;
    private final ManagementFeeMapper managementFeeMapper;
    private final DataChangeHelperMapper dataChangeHelperMapper;

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
        return null;
    }

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

    private ErrorDetail buildError(ManagementFeeRequest request, List<String> errors) {
        return ErrorDetail.builder()
                .field("fundCode")
                .value(request.getFundCode())
                .messages(errors.stream().distinct().toList())
                .build();
    }

}
