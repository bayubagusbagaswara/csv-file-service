package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.exception.DataNotFoundException;
import com.bayu.csvfileservice.model.DataChange;
import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.repository.DataChangeRepository;
import com.bayu.csvfileservice.service.DataChangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.bayu.csvfileservice.model.enumerator.ApprovalStatus.*;
import static com.bayu.csvfileservice.model.enumerator.ChangeAction.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataChangeServiceImpl implements DataChangeService {

    private static final String ID_NOT_FOUND = "Data Change not found with id: ";

    private final DataChangeRepository dataChangeRepository;

    @Override
    public DataChange getById(Long id) {
        return dataChangeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ID_NOT_FOUND + id));
    }

    @Override
    public List<DataChange> getAll() {
        return dataChangeRepository.findAll();
    }

    @Override
    public DataChange getPendingById(Long id) {
        DataChange dataChange = dataChangeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DataChange not found"));
        if (dataChange.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException(
                    "DataChange with id " + id + " already processed"
            );
        }
        return dataChange;
    }

    @Override
    public void setApprovalStatusIsRejected(DataChange dataChange, List<String> messages) {
        DataChange entity = getById(dataChange.getId());
        entity.setApprovalStatus(REJECTED);
        entity.setApproveId(dataChange.getApproveId());
        entity.setApproveIpAddress(dataChange.getApproveId());
        entity.setApproveDate(Optional.ofNullable(dataChange.getApproveDate()).orElse(LocalDateTime.now()));
        entity.setJsonDataAfter(Optional.ofNullable(dataChange.getJsonDataAfter()).orElse(""));
        entity.setJsonDataBefore(Optional.ofNullable(dataChange.getJsonDataBefore()).orElse(""));
        entity.setEntityId(Optional.ofNullable(dataChange.getEntityId()).orElse(""));
        entity.setDescription(joinStrings(messages));

        dataChangeRepository.save(entity);
        log.info("Successfully set approval status Rejected with id: {}", entity.getId());
    }

    @Override
    public void setApprovalStatusIsApproved(DataChange dataChange) {
        DataChange entity = getById(dataChange.getId());
        entity.setApprovalStatus(APPROVED);
        entity.setApproveId(dataChange.getApproveId());
        entity.setApproveIpAddress(dataChange.getApproveId());
        entity.setApproveDate(Optional.ofNullable(dataChange.getApproveDate()).orElse(LocalDateTime.now()));
        entity.setJsonDataAfter(Optional.ofNullable(dataChange.getJsonDataAfter()).orElse(""));
        entity.setJsonDataBefore(Optional.ofNullable(dataChange.getJsonDataBefore()).orElse(""));
        entity.setEntityId(Optional.ofNullable(dataChange.getEntityId()).orElse(""));
        entity.setDescription(dataChange.getDescription());

        dataChangeRepository.save(entity);
        log.info("Successfully set approval status Approved with id: {}", entity.getId());
    }

    @Override
    public <T> void createChangeActionAdd(DataChange dataChange, Class<T> clazz) {
        dataChange.setApprovalStatus(PENDING);
        dataChange.setApproveId("");
        dataChange.setApproveIpAddress("");
        dataChange.setApproveDate(null);

        dataChange.setAction(ADD);
        dataChange.setEntityName(clazz.getName());
        dataChange.setInputDate(Optional.ofNullable(dataChange.getInputDate()).orElse(LocalDateTime.now()));
        dataChange.setDescription("");

        dataChange.setJsonDataBefore("");

        dataChangeRepository.save(dataChange);
        log.info("Successfully save create change action Add with id: {}", dataChange.getId());
    }

    @Override
    public <T> void createChangeActionEdit(DataChange dataChange, Class<T> clazz) {
        dataChange.setApprovalStatus(PENDING);
        dataChange.setApproveId("");
        dataChange.setApproveIpAddress("");
        dataChange.setApproveDate(null);

        dataChange.setAction(EDIT);
        dataChange.setEntityName(clazz.getName());
        dataChange.setInputDate(Optional.ofNullable(dataChange.getInputDate()).orElse(LocalDateTime.now()));
        dataChange.setDescription("");

        dataChangeRepository.save(dataChange);
        log.info("Successfully save create change action Edit with id: {}", dataChange.getId());
    }

    @Override
    public <T> void createChangeActionDelete(DataChange dataChange, Class<T> clazz) {
        dataChange.setApprovalStatus(PENDING);
        dataChange.setApproveId("");
        dataChange.setApproveIpAddress("");
        dataChange.setApproveDate(null);

        dataChange.setAction(DELETE);
        dataChange.setEntityName(clazz.getName());
        dataChange.setInputDate(Optional.ofNullable(dataChange.getInputDate()).orElse(LocalDateTime.now()));
        dataChange.setDescription("");

        dataChangeRepository.save(dataChange);
        log.info("Successfully save create change action Delete with id: {}", dataChange.getId());
    }

    private static String joinStrings(List<String> strings) {
        return String.join(", ", strings);
    }

    // ======================= OPTIONAL HELPER =======================

    public void validatePending(DataChange dataChange) {
        if (dataChange.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException(
                    "DataChange with id " + dataChange.getId() + " already processed"
            );
        }
    }

}
