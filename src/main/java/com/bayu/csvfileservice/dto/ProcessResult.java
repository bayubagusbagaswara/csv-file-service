package com.bayu.csvfileservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessResult {

    private int totalSuccess;
    private int totalFailed;

    @Builder.Default
    private List<ErrorDetail> errors = new ArrayList<>();

    public void addError(ErrorDetail error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
        this.totalFailed++;
    }

    public void addSuccess() {
        this.totalSuccess++;
    }

    public boolean hasError() {
        return totalFailed > 0;
    }

}
