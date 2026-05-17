package com.bayu.csvfileservice.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TransferOrchestratorService {

    private final List<TransferExecutor> executors;

    public TransferOrchestratorService(List<TransferExecutor> executors) {
        this.executors = executors;
    }

    public TransferExecutionResult execute(Transferable item) {

        for (TransferExecutor executor : executors) {
            if (executor.supports(item.getTransferMethod())) {
                return executor.execute(item);
            }
        }

        throw new IllegalStateException(
                "No executor found for transferMethod: " + item.getTransferMethod()
        );
    }

}
