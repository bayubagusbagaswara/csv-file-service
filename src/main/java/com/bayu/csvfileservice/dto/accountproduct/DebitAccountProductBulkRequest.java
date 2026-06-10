package com.bayu.csvfileservice.dto.accountproduct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebitAccountProductBulkRequest {

    private String inputId;

    private List<DebitAccountProductRequest> items;

}