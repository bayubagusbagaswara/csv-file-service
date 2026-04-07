package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.model.DataChange;

import java.util.List;

public interface DataChangeService {

    DataChange getById(Long id);

    DataChange getPendingById(Long id);

    void setApprovalStatusIsRejected(DataChange dataChange, List<String> messages);

    void setApprovalStatusIsApproved(DataChange dataChange);

    <T> void createChangeActionAdd(DataChange dataChange, Class<T> clazz);

    <T> void createChangeActionEdit(DataChange dataChange, Class<T> clazz);

    <T> void createChangeActionDelete(DataChange dataChange, Class<T> clazz);

}
