package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ApproveDataChangeRequest;
import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.authorization.UserLimitAuthorizationBulkRequest;
import com.bayu.csvfileservice.dto.authorization.UserLimitAuthorizationDto;
import com.bayu.csvfileservice.dto.authorization.UserLimitAuthorizationRequest;
import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.exception.DataNotFoundException;
import com.bayu.csvfileservice.mapper.DataChangeHelperMapper;
import com.bayu.csvfileservice.mapper.DataChangeMapper;
import com.bayu.csvfileservice.mapper.UserLimitAuthorizationMapper;
import com.bayu.csvfileservice.model.DataChange;
import com.bayu.csvfileservice.model.UserLimitAuthorization;
import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.repository.UserLimitAuthorizationRepository;
import com.bayu.csvfileservice.service.DataChangeService;
import com.bayu.csvfileservice.service.UserLimitAuthorizationService;
import com.bayu.csvfileservice.util.JsonHelper;
import com.bayu.csvfileservice.util.ValidationData;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLimitAuthorizationServiceImpl implements UserLimitAuthorizationService {

    private static final String MENU_USER_LIMIT_AUTHORIZATION = "User Limit Authorization";
    private static final String BASE_ENDPOINT = "/api/user-limit-authorization";
    private static final String USER_ID = "userId";
    private static final String DATA_CHANGE_ID = "dataChangeId";

    private final UserLimitAuthorizationRepository repository;
    private final UserLimitAuthorizationMapper mapper;
    private final ValidationData validationData;
    private final DataChangeService dataChangeService;
    private final DataChangeMapper dataChangeMapper;
    private final DataChangeHelperMapper dataChangeHelperMapper;
    private final JsonHelper jsonHelper;

    @Override
    public ProcessResult uploadData(UserLimitAuthorizationBulkRequest request, String clientIp) {
        ProcessResult result = new ProcessResult();

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            result.addError(
                    ErrorDetail.of(
                            "items",
                            null,
                            Collections.singletonList("UserLimitAuthorization item list cannot be empty")
                    )
            );
            return result;
        }

        for (UserLimitAuthorizationRequest item : request.getItems()) {

            ErrorDetail errorDetail = processSingleUpload(
                    item,
                    request.getInputId(),
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

    @Override
    public ProcessResult approveCreate(ApproveDataChangeRequest request, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult result = new ProcessResult();

        String userId = null;

        try {
            validateApproveRequest(request);

            DataChange dataChange =
                    dataChangeService.getPendingById(request.getDataChangeId());

            UserLimitAuthorizationDto dto =
                    jsonHelper.fromJson(
                            dataChange.getJsonDataAfter(),
                            UserLimitAuthorizationDto.class
                    );

            userId = dto.getUserId();

            validateCreateDto(dto);

            if (repository.existsByUserId(userId)) {

                List<String> errors = Collections.singletonList(
                        "UserLimitAuthorization already exists for userId: " + userId
                );

                setApprovalFieldsToDataChange(
                        dataChange,
                        request.getApproveId(),
                        clientIp,
                        null,
                        now
                );

                dataChange.setDescription(
                        "Rejected create UserLimitAuthorization because userId already exists: " + userId
                );

                dataChangeService.setApprovalStatusIsRejected(dataChange, errors);

                result.addError(
                        ErrorDetail.of(
                                USER_ID,
                                userId,
                                errors
                        )
                );

                return result;
            }

            UserLimitAuthorization entity = UserLimitAuthorization.builder()
                    .userId(dto.getUserId())
                    .username(dto.getUsername())
                    .userGroup(dto.getUserGroup())
                    .limit(parseLimit(dto.getLimit()))
                    .build();

            entity.setInputId(dataChange.getInputId());
            entity.setInputDate(dataChange.getInputDate());
            entity.setInputIpAddress(dataChange.getInputIpAddress());

            entity.setApprovalStatus(ApprovalStatus.APPROVED);
            entity.setApproveId(request.getApproveId());
            entity.setApproveDate(now);
            entity.setApproveIpAddress(clientIp);

            UserLimitAuthorization saved = repository.save(entity);

            UserLimitAuthorizationDto completeDto =
                    mapper.fromEntityToDto(saved);

            dataChange.setJsonDataAfter(jsonHelper.toJson(completeDto));
            dataChange.setDescription(
                    "Success approve create UserLimitAuthorization with id: " + saved.getId()
            );

            setApprovalFieldsToDataChange(
                    dataChange,
                    request.getApproveId(),
                    clientIp,
                    saved.getId(),
                    now
            );

            dataChangeService.setApprovalStatusIsApproved(dataChange);

            result.addSuccess();

        } catch (Exception e) {
            log.error(
                    "Failed to approve create UserLimitAuthorization. dataChangeId={}, userId={}",
                    request != null ? request.getDataChangeId() : null,
                    userId,
                    e
            );

            result.addError(
                    ErrorDetail.of(
                            DATA_CHANGE_ID,
                            request != null && request.getDataChangeId() != null
                                    ? String.valueOf(request.getDataChangeId())
                                    : null,
                            Collections.singletonList(getErrorMessage(e))
                    )
            );
        }

        return result;
    }

    @Override
    public ProcessResult approveEdit(ApproveDataChangeRequest request, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult result = new ProcessResult();

        String userId = null;

        try {
            validateApproveRequest(request);

            DataChange dataChange =
                    dataChangeService.getPendingById(request.getDataChangeId());

            UserLimitAuthorizationDto afterDto =
                    jsonHelper.fromJson(
                            dataChange.getJsonDataAfter(),
                            UserLimitAuthorizationDto.class
                    );

            userId = afterDto.getUserId();

            validateEditDto(afterDto);

            String finalUserId = userId;
            UserLimitAuthorization entity =
                    repository.findByUserId(userId)
                            .orElseThrow(() -> new DataNotFoundException(
                                    "UserLimitAuthorization not found for userId: " + finalUserId
                            ));

            if (afterDto.getId() != null && !afterDto.getId().equals(entity.getId())) {
                throw new IllegalStateException(
                        "Data mismatch. DataChange id does not match existing UserLimitAuthorization id"
                );
            }

            // Hanya limit yang boleh diubah.
            entity.setLimit(parseLimit(afterDto.getLimit()));

            entity.setApprovalStatus(ApprovalStatus.APPROVED);
            entity.setApproveId(request.getApproveId());
            entity.setApproveDate(now);
            entity.setApproveIpAddress(clientIp);

            UserLimitAuthorization saved = repository.save(entity);

            UserLimitAuthorizationDto completeDto =
                    mapper.fromEntityToDto(saved);

            dataChange.setJsonDataAfter(jsonHelper.toJson(completeDto));
            dataChange.setDescription(
                    "Success approve edit UserLimitAuthorization limit with id: " + saved.getId()
            );

            setApprovalFieldsToDataChange(
                    dataChange,
                    request.getApproveId(),
                    clientIp,
                    saved.getId(),
                    now
            );

            dataChangeService.setApprovalStatusIsApproved(dataChange);

            result.addSuccess();

        } catch (Exception e) {
            log.error(
                    "Failed to approve edit UserLimitAuthorization. dataChangeId={}, userId={}",
                    request != null ? request.getDataChangeId() : null,
                    userId,
                    e
            );

            result.addError(
                    ErrorDetail.of(
                            DATA_CHANGE_ID,
                            request != null && request.getDataChangeId() != null
                                    ? String.valueOf(request.getDataChangeId())
                                    : null,
                            Collections.singletonList(getErrorMessage(e))
                    )
            );
        }

        return result;
    }

    @Override
    public List<UserLimitAuthorizationDto> getAll() {
        return List.of();
    }

    // --------------------------- Helper -----------------------------------
    private ErrorDetail processSingleUpload(
            UserLimitAuthorizationRequest request,
            String inputId,
            String clientIp
    ) {
        String userId = request != null ? request.getUserId() : null;

        try {
            // 1. Validasi umum menggunakan ValidationData.
            List<String> validationErrors = validateRequest(request);

            if (!validationErrors.isEmpty()) {
                return ErrorDetail.of(
                        USER_ID,
                        userId,
                        validationErrors
                );
            }

            // 2. Setelah validateRequest lolos, request sudah pasti tidak null.
            UserLimitAuthorizationRequest validRequest =
                    Objects.requireNonNull(request, "UserLimitAuthorization request is required");

            // 3. Validasi format limit.
            parseLimit(validRequest.getLimit());

            // 4. Cek apakah userId sudah ada.
            Optional<UserLimitAuthorization> existingOpt =
                    repository.findByUserId(validRequest.getUserId());

            if (existingOpt.isPresent()) {
                handleEditData(validRequest, existingOpt.get(), inputId, clientIp);
            } else {
                handleNewData(validRequest, inputId, clientIp);
            }

            return null;

        } catch (Exception e) {
            log.error(
                    "Failed to upload UserLimitAuthorization. userId={}",
                    userId,
                    e
            );

            return ErrorDetail.of(
                    USER_ID,
                    userId,
                    Collections.singletonList(getErrorMessage(e))
            );
        }
    }

    private void handleNewData(
            UserLimitAuthorizationRequest request,
            String inputId,
            String clientIp
    ) {
        validateCreateRequiredFields(request);

        UserLimitAuthorizationDto afterDto =
                mapper.fromRequestToDto(request);

        DataChangeDto baseDto = buildBaseDataChangeDto(
                inputId,
                clientIp,
                "POST",
                BASE_ENDPOINT + "/approve-create"
        );

        baseDto.setDescription(
                "Request add UserLimitAuthorization for userId: " + request.getUserId()
        );

        DataChangeDto dataChangeDto =
                dataChangeHelperMapper.forAdd(baseDto, afterDto);

        DataChange dataChange =
                dataChangeMapper.toEntity(dataChangeDto);

        dataChangeService.createChangeActionAdd(
                dataChange,
                UserLimitAuthorization.class
        );
    }

    private void handleEditData(
            UserLimitAuthorizationRequest request,
            UserLimitAuthorization existing,
            String inputId,
            String clientIp
    ) {
        UserLimitAuthorizationDto beforeDto = mapper.fromEntityToDto(existing);

        UserLimitAuthorizationDto afterDto = mapper.fromEntityToDto(existing);

        // Hanya limit yang diganti di data after.
        afterDto.setLimit(request.getLimit());

        DataChangeDto baseDto = buildBaseDataChangeDto(
                inputId,
                clientIp,
                "PUT",
                BASE_ENDPOINT + "/approve-edit"
        );

        baseDto.setDescription(
                "Request edit UserLimitAuthorization limit for userId: " + request.getUserId()
        );

        DataChangeDto dataChangeDto =
                dataChangeHelperMapper.forEdit(baseDto, beforeDto, afterDto);

        dataChangeDto.setEntityId(String.valueOf(existing.getId()));

        DataChange dataChange =
                dataChangeMapper.toEntity(dataChangeDto);

        dataChangeService.createChangeActionEdit(
                dataChange,
                UserLimitAuthorization.class
        );
    }

    private DataChangeDto buildBaseDataChangeDto(
            String inputId,
            String clientIp,
            String httpMethod,
            String endpoint
    ) {
        return DataChangeDto.builder()
                .inputId(inputId)
                .inputDate(LocalDateTime.now())
                .inputIpAddress(clientIp)
                .httpMethod(httpMethod)
                .endpoint(endpoint)
                .menu(MENU_USER_LIMIT_AUTHORIZATION)
                .requestBody(true)
                .requestParam(false)
                .pathVariable(false)
                .build();
    }

    private String getErrorMessage(Exception e) {
        if (e == null) {
            return "Failed to process data";
        }

        String message = e.getMessage();

        if (message == null || message.trim().isEmpty()) {
            return "Failed to process data";
        }

        if (message.contains("cannot be cast")
                || message.contains("Cannot cast")) {
            return "Invalid data type. Please check the submitted data format";
        }

        return message;
    }

    private void validateCreateRequiredFields(UserLimitAuthorizationRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("username is required for create");
        }

        if (request.getUserGroup() == null || request.getUserGroup().trim().isEmpty()) {
            throw new IllegalArgumentException("userGroup is required for create");
        }
    }

    private void validateApproveRequest(ApproveDataChangeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Approve request is required");
        }

        if (request.getDataChangeId() == null) {
            throw new IllegalArgumentException("dataChangeId is required");
        }

        if (request.getApproveId() == null || request.getApproveId().trim().isEmpty()) {
            throw new IllegalArgumentException("approveId is required");
        }
    }

    private void validateCreateDto(UserLimitAuthorizationDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("UserLimitAuthorization data is required");
        }

        if (dto.getUserId() == null || dto.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("userId is required");
        }

        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("username is required");
        }

        if (dto.getUserGroup() == null || dto.getUserGroup().trim().isEmpty()) {
            throw new IllegalArgumentException("userGroup is required");
        }

        parseLimit(dto.getLimit());
    }

    private void validateEditDto(UserLimitAuthorizationDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("UserLimitAuthorization edit data is required");
        }

        if (dto.getUserId() == null || dto.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("userId is required");
        }

        parseLimit(dto.getLimit());
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

    private List<String> validateRequest(UserLimitAuthorizationRequest request) {
        List<String> validationErrors = new ArrayList<>();

        if (request == null) {
            validationErrors.add("request : UserLimitAuthorization request is required");
            return validationErrors;
        }

        Set<ConstraintViolation<UserLimitAuthorizationRequest>> constraintViolations = validationData.validateObject(request);

        if (!constraintViolations.isEmpty()) {
            validationErrors.addAll(
                    constraintViolations.stream()
                            .map(v -> v.getPropertyPath() + " : " + v.getMessage())
                            .toList()
            );
        }

        return validationErrors;
    }

    private BigDecimal parseLimit(String limit) {

        if (limit == null || limit.trim().isEmpty()) {
            throw new IllegalArgumentException("limit is required");
        }

        String normalized = limit.trim()
                .replace(",", "");

        BigDecimal value;

        try {
            value = new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid limit format. Example valid format: 0, 1000.00, or 1,000.00"
            );
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("limit must be greater than or equal to 0");
        }

        return value;
    }

}
