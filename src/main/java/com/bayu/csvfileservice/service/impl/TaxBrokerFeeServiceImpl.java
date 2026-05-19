package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ApproveDataChangeRequest;
import com.bayu.csvfileservice.dto.DeleteIdRequest;
import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.dto.taxbrokerfee.TaxBrokerFeeBulkRequest;
import com.bayu.csvfileservice.dto.taxbrokerfee.TaxBrokerFeeDto;
import com.bayu.csvfileservice.dto.taxbrokerfee.TaxBrokerFeeRequest;
import com.bayu.csvfileservice.exception.DataNotFoundException;
import com.bayu.csvfileservice.mapper.DataChangeHelperMapper;
import com.bayu.csvfileservice.mapper.DataChangeMapper;
import com.bayu.csvfileservice.mapper.TaxBrokerFeeMapper;
import com.bayu.csvfileservice.model.DataChange;
import com.bayu.csvfileservice.model.TaxBrokerFee;
import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.repository.TaxBrokerFeeRepository;
import com.bayu.csvfileservice.service.DataChangeService;
import com.bayu.csvfileservice.service.TaxBrokerFeeService;
import com.bayu.csvfileservice.util.JsonHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaxBrokerFeeServiceImpl implements TaxBrokerFeeService {

    private static final String REFERENCE_COMBINATION = "referenceCombination";
    private static final String DATA_CHANGE_ID = "dataChangeId";
    private static final String ID = "id";

    private static final String MENU_TAX_BROKER_FEE = "Tax Broker Fee";
    private static final String BASE_URL_TAX_BROKER_FEE = "/api/tax-broker-fee";

    private final TaxBrokerFeeRepository taxBrokerFeeRepository;
    private final TaxBrokerFeeMapper taxBrokerFeeMapper;

    private final DataChangeService dataChangeService;
    private final DataChangeMapper dataChangeMapper;
    private final DataChangeHelperMapper dataChangeHelperMapper;
    private final JsonHelper jsonHelper;

    @Override
    @Transactional
    public ProcessResult upload(
            TaxBrokerFeeBulkRequest request,
            String clientIp
    ) {
        ProcessResult result = new ProcessResult();

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            result.addError(
                    ErrorDetail.of(
                            "items",
                            null,
                            Collections.singletonList("TaxBrokerFee item list cannot be empty")
                    )
            );
            return result;
        }

        for (TaxBrokerFeeRequest item : request.getItems()) {
            ErrorDetail errorDetail = processSingleUpload(
                    item,
                    request.getUserId(),
                    clientIp
            );

            if (errorDetail != null) {
                result.addError(errorDetail);
            } else {
                result.addSuccess();
            }
        }

        return result;
    }

    private ErrorDetail processSingleUpload(
            TaxBrokerFeeRequest request,
            String userId,
            String clientIp
    ) {
        List<String> errors = new ArrayList<String>();

        try {
            validateRequest(request);

            TaxBrokerFeeDto dto = taxBrokerFeeMapper.fromRequestToDto(request);

            DataChangeDto baseDto = buildDataChangeDto(
                    userId,
                    clientIp,
                    "POST",
                    BASE_URL_TAX_BROKER_FEE + "/upload",
                    null
            );

            DataChangeDto dtoAudit = dataChangeHelperMapper.forAdd(baseDto, dto);

            DataChange dataChange = dataChangeMapper.toEntity(dtoAudit);

            dataChangeService.createChangeActionAdd(
                    dataChange,
                    TaxBrokerFee.class
            );

            return null;

        } catch (Exception e) {
            log.error(
                    "Failed to upload TaxBrokerFee. referenceCombination={}",
                    request != null ? request.getReferenceCombination() : null,
                    e
            );

            errors.add(e.getMessage());

            return ErrorDetail.of(
                    REFERENCE_COMBINATION,
                    request != null ? request.getReferenceCombination() : null,
                    errors
            );
        }
    }

    @Override
    @Transactional
    public ProcessResult createApprove(
            ApproveDataChangeRequest request,
            String clientIp
    ) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult result = new ProcessResult();
        String referenceCombination = null;

        try {
            Long dataChangeId = request.getDataChangeId();

            DataChange dataChange = dataChangeService.getPendingById(dataChangeId);

            TaxBrokerFeeDto dto = jsonHelper.fromJson(
                    dataChange.getJsonDataAfter(),
                    TaxBrokerFeeDto.class
            );

            referenceCombination = dto.getReferenceCombination();

            if (taxBrokerFeeRepository.existsByReferenceCombination(referenceCombination)) {
                List<String> errors = Collections.singletonList(
                        "TaxBrokerFee already exists for referenceCombination '" + referenceCombination + "'"
                );

                setApprovalFieldsToDataChange(
                        dataChange,
                        request.getApproveId(),
                        clientIp,
                        null,
                        now
                );

                dataChange.setDescription(
                        "Rejected because TaxBrokerFee already exists for referenceCombination: "
                                + referenceCombination
                );

                dataChangeService.setApprovalStatusIsRejected(dataChange, errors);

                result.addError(
                        ErrorDetail.of(
                                REFERENCE_COMBINATION,
                                referenceCombination,
                                errors
                        )
                );

                return result;
            }

            TaxBrokerFee entity = taxBrokerFeeMapper.toEntity(dto);

            setApprovalFields(entity, dataChange, request.getApproveId(), clientIp, now);

            TaxBrokerFee saved = taxBrokerFeeRepository.save(entity);

            setApprovalFieldsToDataChange(
                    dataChange,
                    request.getApproveId(),
                    clientIp,
                    saved.getId(),
                    now
            );

            TaxBrokerFeeDto completeDto = taxBrokerFeeMapper.toDto(saved);

            dataChange.setJsonDataAfter(jsonHelper.toJson(completeDto));
            dataChange.setDescription(
                    "Success approve insert of TaxBrokerFee with id: " + saved.getId()
            );

            dataChangeService.setApprovalStatusIsApproved(dataChange);

            result.addSuccess();

        } catch (Exception e) {
            log.error(
                    "Failed to approve TaxBrokerFee create. dataChangeId={}, referenceCombination={}",
                    request != null ? request.getDataChangeId() : null,
                    referenceCombination,
                    e
            );

            result.addError(
                    ErrorDetail.of(
                            DATA_CHANGE_ID,
                            request != null ? String.valueOf(request.getDataChangeId()) : null,
                            Collections.singletonList("Failed to approve TaxBrokerFee data")
                    )
            );
        }

        return result;
    }

    @Override
    @Transactional
    public ProcessResult deleteById(
            DeleteIdRequest request,
            String clientIp
    ) {
        ProcessResult result = new ProcessResult();

        try {
            if (request == null || request.getId() == null) {
                throw new IllegalArgumentException("id is required");
            }

            TaxBrokerFee entity = taxBrokerFeeRepository.findById(request.getId())
                    .orElseThrow(() -> new DataNotFoundException(
                            "TaxBrokerFee not found with id: " + request.getId()
                    ));

            TaxBrokerFeeDto dto = taxBrokerFeeMapper.toDto(entity);

            DataChangeDto baseDto = buildDataChangeDto(
                    request.getInputId(),
                    clientIp,
                    "DELETE",
                    BASE_URL_TAX_BROKER_FEE + "/delete",
                    String.valueOf(request.getId())
            );

            DataChangeDto dtoAudit = dataChangeHelperMapper.forDelete(baseDto, dto);

            DataChange dataChange = dataChangeMapper.toEntity(dtoAudit);

            dataChangeService.createChangeActionDelete(
                    dataChange,
                    TaxBrokerFee.class
            );

            result.addSuccess();

        } catch (Exception e) {
            log.error(
                    "Failed to create TaxBrokerFee delete request. id={}",
                    request != null ? request.getId() : null,
                    e
            );

            result.addError(
                    ErrorDetail.of(
                            ID,
                            request != null && request.getId() != null
                                    ? String.valueOf(request.getId())
                                    : null,
                            Collections.singletonList(e.getMessage())
                    )
            );
        }

        return result;
    }

    @Override
    @Transactional
    public ProcessResult deleteApprove(
            ApproveDataChangeRequest request,
            String clientIp
    ) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult result = new ProcessResult();

        try {
            Long dataChangeId = request.getDataChangeId();

            DataChange dataChange = dataChangeService.getPendingById(dataChangeId);

            Long entityId = dataChange.getEntityId() != null
                    ? Long.valueOf(dataChange.getEntityId())
                    : null;

            Optional<TaxBrokerFee> optional = entityId != null
                    ? taxBrokerFeeRepository.findById(entityId)
                    : Optional.empty();

            if (optional.isPresent()) {
                TaxBrokerFee entity = optional.get();

                setApprovalFieldsToDataChange(
                        dataChange,
                        request.getApproveId(),
                        clientIp,
                        entity.getId(),
                        now
                );

                taxBrokerFeeRepository.delete(entity);

                dataChange.setJsonDataAfter(null);
                dataChange.setDescription(
                        "Success approve delete of TaxBrokerFee with id: " + entity.getId()
                );

                dataChangeService.setApprovalStatusIsApproved(dataChange);

                result.addSuccess();

            } else {
                List<String> errors = Collections.singletonList("TaxBrokerFee not found");

                setApprovalFieldsToDataChange(
                        dataChange,
                        request.getApproveId(),
                        clientIp,
                        null,
                        now
                );

                dataChangeService.setApprovalStatusIsRejected(dataChange, errors);

                result.addError(
                        ErrorDetail.of(
                                DATA_CHANGE_ID,
                                String.valueOf(dataChangeId),
                                errors
                        )
                );
            }

        } catch (Exception e) {
            log.error(
                    "Failed to approve TaxBrokerFee delete. dataChangeId={}",
                    request != null ? request.getDataChangeId() : null,
                    e
            );

            result.addError(
                    ErrorDetail.of(
                            DATA_CHANGE_ID,
                            request != null ? String.valueOf(request.getDataChangeId()) : null,
                            Collections.singletonList("Failed to approve TaxBrokerFee delete")
                    )
            );
        }

        return result;
    }

    @Override
    public List<TaxBrokerFeeDto> getAllByDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date is required");
        }

        List<TaxBrokerFee> entities = taxBrokerFeeRepository.findAllByDate(date);

        return taxBrokerFeeMapper.toDtos(entities);
    }

    private void validateRequest(TaxBrokerFeeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("TaxBrokerFee request is required");
        }

        if (request.getDate() == null) {
            throw new IllegalArgumentException("date is required");
        }

        if (request.getReferenceCombination() == null
                || request.getReferenceCombination().trim().isEmpty()) {
            throw new IllegalArgumentException("referenceCombination is required");
        }

        if (request.getDebitAccount() == null || request.getDebitAccount().trim().isEmpty()) {
            throw new IllegalArgumentException("debitAccount is required");
        }

        if (request.getCreditAccount() == null || request.getCreditAccount().trim().isEmpty()) {
            throw new IllegalArgumentException("creditAccount is required");
        }
    }

    private DataChangeDto buildDataChangeDto(
            String userId,
            String clientIp,
            String httpMethod,
            String endpoint,
            String pathVariable
    ) {
        return DataChangeDto.builder()
                .inputId(userId)
                .inputIpAddress(clientIp)
                .inputDate(LocalDateTime.now())
                .httpMethod(httpMethod)
                .endpoint(endpoint)
                .menu(MENU_TAX_BROKER_FEE)
                .requestBody(true)
                .requestParam(false)
                .pathVariable(pathVariable != null)
                .build();
    }

    private void setApprovalFields(
            TaxBrokerFee entity,
            DataChange dataChange,
            String approveId,
            String clientIp,
            LocalDateTime now
    ) {
        entity.setInputId(dataChange.getInputId());
        entity.setInputDate(dataChange.getInputDate());
        entity.setInputIpAddress(dataChange.getInputIpAddress());

        entity.setApprovalStatus(ApprovalStatus.APPROVED);
        entity.setApproveId(approveId);
        entity.setApproveDate(now);
        entity.setApproveIpAddress(clientIp);
    }

    private void setApprovalFieldsToDataChange(
            DataChange dataChange,
            String approveId,
            String clientIp,
            Long entityId,
            LocalDateTime now
    ) {
        dataChange.setApproveId(approveId);
        dataChange.setApproveDate(now);
        dataChange.setApproveIpAddress(clientIp);

        if (entityId != null) {
            dataChange.setEntityId(String.valueOf(entityId));
        }
    }
}
