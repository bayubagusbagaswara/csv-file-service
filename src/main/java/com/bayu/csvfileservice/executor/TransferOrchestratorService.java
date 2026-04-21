package com.bayu.csvfileservice.executor;

import com.bayu.csvfileservice.model.NcbsResponse;
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

    public NcbsResponse execute(Transferable item) {
        TransferExecutor executor = null;
        for (TransferExecutor e : executors) {
            if (e.supports(item.getTransferMethod())) {
                executor = e;
                break;
            }
        }

        if (executor == null) {
            throw new IllegalStateException("No executor found");
        }

        return executor.execute(item);
    }

}
