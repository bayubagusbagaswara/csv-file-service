package com.bayu.csvfileservice.executor;

import com.bayu.csvfileservice.model.enumerator.TransferMethod;

public interface TransferExecutor {

    boolean supports(TransferMethod transferMethod);

    TransferExecutionResult execute(Transferable item);

}
