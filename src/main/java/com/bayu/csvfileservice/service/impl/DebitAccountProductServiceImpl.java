package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ApproveDataChangeRequest;
import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.dto.accountproduct.DebitAccountProductBulkRequest;
import com.bayu.csvfileservice.dto.accountproduct.DebitAccountProductDto;
import com.bayu.csvfileservice.dto.accountproduct.DebitAccountProductRequest;
import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.exception.DataNotFoundException;
import com.bayu.csvfileservice.mapper.DataChangeHelperMapper;
import com.bayu.csvfileservice.mapper.DataChangeMapper;
import com.bayu.csvfileservice.mapper.DebitAccountProductMapper;
import com.bayu.csvfileservice.model.DataChange;
import com.bayu.csvfileservice.model.DebitAccountProduct;
import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.repository.DebitAccountProductRepository;
import com.bayu.csvfileservice.service.DataChangeService;
import com.bayu.csvfileservice.service.DebitAccountProductService;
import com.bayu.csvfileservice.util.JsonHelper;
import com.bayu.csvfileservice.util.ValidationData;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DebitAccountProductServiceImpl implements DebitAccountProductService {

    private static final String PRODUCT_CODE = "productCode";
    private static final String DATA_CHANGE_ID = "dataChangeId";
    private static final String ITEMS = "items";

    private static final String MENU_DEBIT_ACCOUNT_PRODUCT = "Debit Account Product";
    private static final String BASE_ENDPOINT = "/api/debit-account-products";

    private final DebitAccountProductRepository repository;
    private final DebitAccountProductMapper mapper;

    private final DataChangeService dataChangeService;
    private final DataChangeMapper dataChangeMapper;
    private final DataChangeHelperMapper dataChangeHelperMapper;
    private final JsonHelper jsonHelper;
    private final ValidationData validationData;

    @Override
    @Transactional
    public ProcessResult uploadData(
            DebitAccountProductBulkRequest request,
            String clientIp
    ) {
        ProcessResult result = new ProcessResult();

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            result.addError(
                    ErrorDetail.of(
                            ITEMS,
                            null,
                            Collections.singletonList("DebitAccountProduct item list cannot be empty")
                    )
            );
            return result;
        }

        for (DebitAccountProductRequest item : request.getItems()) {
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

    private ErrorDetail processSingleUpload(
            DebitAccountProductRequest request,
            String inputId,
            String clientIp
    ) {
        String productCode = request != null ? request.getProductCode() : null;

        try {
            List<String> validationErrors = validateRequest(request);

            if (!validationErrors.isEmpty()) {
                return ErrorDetail.of(
                        PRODUCT_CODE,
                        productCode,
                        validationErrors
                );
            }

            DebitAccountProductRequest validRequest =
                    Objects.requireNonNull(request, "DebitAccountProduct request is required");

            Optional<DebitAccountProduct> existingOpt =
                    repository.findByProductCode(validRequest.getProductCode());

            if (existingOpt.isPresent()) {
                handleEditData(validRequest, existingOpt.get(), inputId, clientIp);
            } else {
                handleNewData(validRequest, inputId, clientIp);
            }

            return null;

        } catch (Exception e) {
            log.error(
                    "Failed to upload DebitAccountProduct. productCode={}, fundCode={}",
                    productCode,
                    request != null ? request.getFundCode() : null,
                    e
            );

            return ErrorDetail.of(
                    PRODUCT_CODE,
                    productCode,
                    Collections.singletonList(getErrorMessage(e))
            );
        }
    }

    private void handleNewData(
            DebitAccountProductRequest request,
            String inputId,
            String clientIp
    ) {
        validateCreateRequiredFields(request);

        DebitAccountProductDto afterDto =
                mapper.fromRequestToDto(request);

        DataChangeDto baseDto = buildBaseDataChangeDto(
                inputId,
                clientIp,
                "POST",
                BASE_ENDPOINT + "/approve-create"
        );

        baseDto.setDescription(
                "Request add DebitAccountProduct for productCode: " + request.getProductCode()
        );

        DataChangeDto dataChangeDto =
                dataChangeHelperMapper.forAdd(baseDto, afterDto);

        DataChange dataChange =
                dataChangeMapper.toEntity(dataChangeDto);

        dataChangeService.createChangeActionAdd(
                dataChange,
                DebitAccountProduct.class
        );
    }

    private void handleEditData(
            DebitAccountProductRequest request,
            DebitAccountProduct existing,
            String inputId,
            String clientIp
    ) {
        DebitAccountProductDto beforeDto =
                mapper.fromEntityToDto(existing);

        DebitAccountProductDto afterDto =
                buildAfterEditDto(existing, request);

        DataChangeDto baseDto = buildBaseDataChangeDto(
                inputId,
                clientIp,
                "PUT",
                BASE_ENDPOINT + "/approve-edit"
        );

        baseDto.setDescription(
                "Request edit DebitAccountProduct for productCode: " + existing.getProductCode()
        );

        DataChangeDto dataChangeDto =
                dataChangeHelperMapper.forEdit(baseDto, beforeDto, afterDto);

        dataChangeDto.setEntityId(String.valueOf(existing.getId()));

        DataChange dataChange =
                dataChangeMapper.toEntity(dataChangeDto);

        dataChangeService.createChangeActionEdit(
                dataChange,
                DebitAccountProduct.class
        );
    }

    private DebitAccountProductDto buildAfterEditDto(
            DebitAccountProduct existing,
            DebitAccountProductRequest request
    ) {
        DebitAccountProductDto afterDto =
                mapper.fromEntityToDto(existing);

        // productCode tidak boleh berubah.
        // Jadi afterDto.productCode tetap dari existing data.

        if (hasText(request.getFundCode())) {
            afterDto.setFundCode(request.getFundCode());
        }

        if (hasText(request.getFundName())) {
            afterDto.setFundName(request.getFundName());
        }

        if (hasText(request.getImCode())) {
            afterDto.setImCode(request.getImCode());
        }

        if (hasText(request.getImName())) {
            afterDto.setImName(request.getImName());
        }

        if (hasText(request.getCurrency())) {
            afterDto.setCurrency(request.getCurrency());
        }

        if (hasText(request.getCashAccount())) {
            afterDto.setCashAccount(request.getCashAccount());
        }

        if (hasText(request.getBankName())) {
            afterDto.setBankName(request.getBankName());
        }

        return afterDto;
    }

    @Override
    @Transactional
    public ProcessResult approveCreate(
            ApproveDataChangeRequest request,
            String clientIp
    ) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult result = new ProcessResult();

        Long dataChangeId = request != null ? request.getDataChangeId() : null;
        String productCode = null;

        try {
            validateApproveRequest(request);

            DataChange dataChange =
                    dataChangeService.getPendingById(request.getDataChangeId());

            DebitAccountProductDto dto =
                    jsonHelper.fromJson(
                            dataChange.getJsonDataAfter(),
                            DebitAccountProductDto.class
                    );

            validateCreateDto(dto);

            productCode = dto.getProductCode();

            if (repository.existsByProductCode(productCode)) {
                List<String> errors = Collections.singletonList(
                        "DebitAccountProduct already exists for productCode: " + productCode
                );

                setApprovalFieldsToDataChange(
                        dataChange,
                        request.getApproveId(),
                        clientIp,
                        null,
                        now
                );

                dataChange.setDescription(
                        "Rejected create DebitAccountProduct because productCode already exists: " + productCode
                );

                dataChangeService.setApprovalStatusIsRejected(dataChange, errors);

                result.addError(
                        ErrorDetail.of(
                                PRODUCT_CODE,
                                productCode,
                                errors
                        )
                );

                return result;
            }

            DebitAccountProduct entity =
                    mapper.toEntity(dto);

            entity.setInputId(dataChange.getInputId());
            entity.setInputDate(dataChange.getInputDate());
            entity.setInputIpAddress(dataChange.getInputIpAddress());

            entity.setApprovalStatus(ApprovalStatus.APPROVED);
            entity.setApproveId(request.getApproveId());
            entity.setApproveDate(now);
            entity.setApproveIpAddress(clientIp);

            DebitAccountProduct saved =
                    repository.save(entity);

            DebitAccountProductDto completeDto =
                    mapper.fromEntityToDto(saved);

            dataChange.setJsonDataAfter(jsonHelper.toJson(completeDto));
            dataChange.setDescription(
                    "Success approve create DebitAccountProduct with id: " + saved.getId()
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
                    "Failed to approve create DebitAccountProduct. dataChangeId={}, productCode={}",
                    dataChangeId,
                    productCode,
                    e
            );

            result.addError(
                    ErrorDetail.of(
                            DATA_CHANGE_ID,
                            dataChangeId != null ? String.valueOf(dataChangeId) : null,
                            Collections.singletonList(getErrorMessage(e))
                    )
            );
        }

        return result;
    }

    @Override
    @Transactional
    public ProcessResult approveEdit(
            ApproveDataChangeRequest request,
            String clientIp
    ) {
        LocalDateTime now = LocalDateTime.now();
        ProcessResult result = new ProcessResult();

        Long dataChangeId = request != null ? request.getDataChangeId() : null;
        String productCode = null;

        try {
            validateApproveRequest(request);

            DataChange dataChange =
                    dataChangeService.getPendingById(request.getDataChangeId());

            DebitAccountProductDto afterDto =
                    jsonHelper.fromJson(
                            dataChange.getJsonDataAfter(),
                            DebitAccountProductDto.class
                    );

            validateEditDto(afterDto);

            productCode = afterDto.getProductCode();

            String finalProductCode = productCode;
            DebitAccountProduct entity =
                    repository.findByProductCode(productCode)
                            .orElseThrow(() -> new DataNotFoundException(
                                    "DebitAccountProduct not found for productCode: " + finalProductCode
                            ));

            if (afterDto.getId() != null && !afterDto.getId().equals(entity.getId())) {
                throw new IllegalStateException(
                        "Data mismatch. DataChange id does not match existing DebitAccountProduct id"
                );
            }

            // productCode tidak diubah.
            entity.setFundCode(afterDto.getFundCode());
            entity.setFundName(afterDto.getFundName());
            entity.setImCode(afterDto.getImCode());
            entity.setImName(afterDto.getImName());
            entity.setCurrency(afterDto.getCurrency());
            entity.setCashAccount(afterDto.getCashAccount());
            entity.setBankName(afterDto.getBankName());

            entity.setApprovalStatus(ApprovalStatus.APPROVED);
            entity.setApproveId(request.getApproveId());
            entity.setApproveDate(now);
            entity.setApproveIpAddress(clientIp);

            DebitAccountProduct saved =
                    repository.save(entity);

            DebitAccountProductDto completeDto =
                    mapper.fromEntityToDto(saved);

            dataChange.setJsonDataAfter(jsonHelper.toJson(completeDto));
            dataChange.setDescription(
                    "Success approve edit DebitAccountProduct with id: " + saved.getId()
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
                    "Failed to approve edit DebitAccountProduct. dataChangeId={}, productCode={}",
                    dataChangeId,
                    productCode,
                    e
            );

            result.addError(
                    ErrorDetail.of(
                            DATA_CHANGE_ID,
                            dataChangeId != null ? String.valueOf(dataChangeId) : null,
                            Collections.singletonList(getErrorMessage(e))
                    )
            );
        }

        return result;
    }

    @Override
    public List<DebitAccountProductDto> getAll() {
        List<DebitAccountProduct> entities =
                repository.findAll();

        return mapper.fromEntitiesToDtos(entities);
    }

    private List<String> validateRequest(DebitAccountProductRequest request) {
        List<String> validationErrors = new ArrayList<>();

        if (request == null) {
            validationErrors.add("request : DebitAccountProduct request is required");
            return validationErrors;
        }

        Set<ConstraintViolation<DebitAccountProductRequest>> constraintViolations =
                validationData.validateObject(request);

        if (!constraintViolations.isEmpty()) {
            validationErrors.addAll(
                    constraintViolations.stream()
                            .map(v -> v.getPropertyPath() + " : " + v.getMessage())
                            .toList()
            );
        }

        return validationErrors;
    }

    private void validateCreateRequiredFields(DebitAccountProductRequest request) {
        List<String> errors = new ArrayList<>();

        if (!hasText(request.getProductCode())) {
            errors.add("productCode is required for create");
        }

        if (!hasText(request.getFundCode())) {
            errors.add("fundCode is required for create");
        }

        if (!hasText(request.getFundName())) {
            errors.add("fundName is required for create");
        }

        if (!hasText(request.getImCode())) {
            errors.add("imCode is required for create");
        }

        if (!hasText(request.getImName())) {
            errors.add("imName is required for create");
        }

        if (!hasText(request.getCurrency())) {
            errors.add("currency is required for create");
        }

        if (!hasText(request.getCashAccount())) {
            errors.add("cashAccount is required for create");
        }

        if (!hasText(request.getBankName())) {
            errors.add("bankName is required for create");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }

    private void validateCreateDto(DebitAccountProductDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DebitAccountProduct data is required");
        }

        if (!hasText(dto.getProductCode())) {
            throw new IllegalArgumentException("productCode is required");
        }

        if (!hasText(dto.getFundCode())) {
            throw new IllegalArgumentException("fundCode is required");
        }

        if (!hasText(dto.getFundName())) {
            throw new IllegalArgumentException("fundName is required");
        }

        if (!hasText(dto.getImCode())) {
            throw new IllegalArgumentException("imCode is required");
        }

        if (!hasText(dto.getImName())) {
            throw new IllegalArgumentException("imName is required");
        }

        if (!hasText(dto.getCurrency())) {
            throw new IllegalArgumentException("currency is required");
        }

        if (!hasText(dto.getCashAccount())) {
            throw new IllegalArgumentException("cashAccount is required");
        }

        if (!hasText(dto.getBankName())) {
            throw new IllegalArgumentException("bankName is required");
        }
    }

    private void validateEditDto(DebitAccountProductDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DebitAccountProduct edit data is required");
        }

        if (dto.getId() == null) {
            throw new IllegalArgumentException("id is required for edit");
        }

        validateCreateDto(dto);
    }

    private void validateApproveRequest(ApproveDataChangeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Approve request is required");
        }

        if (request.getDataChangeId() == null) {
            throw new IllegalArgumentException("dataChangeId is required");
        }

        if (!hasText(request.getApproveId())) {
            throw new IllegalArgumentException("approveId is required");
        }
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
                .menu(MENU_DEBIT_ACCOUNT_PRODUCT)
                .requestBody(true)
                .requestParam(false)
                .pathVariable(false)
                .build();
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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
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
}