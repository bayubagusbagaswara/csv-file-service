package com.bayu.csvfileservice.executor;

import com.bayu.csvfileservice.model.enumerator.FeatureType;
import com.bayu.csvfileservice.model.enumerator.ProcessType;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import com.bayu.csvfileservice.model.enumerator.TransferScope;

import java.math.BigDecimal;

public interface Transferable {

    Long getId();

    String getDebitAccount();

    String getCreditAccount();

    BigDecimal getAmount();

    String getDescription();

    TransferMethod getTransferMethod();

    TransferScope getTransferScope();

    ProcessType getProcessType();

    FeatureType getFeatureType();

}
