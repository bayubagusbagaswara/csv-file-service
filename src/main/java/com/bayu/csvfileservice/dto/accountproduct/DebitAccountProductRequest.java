package com.bayu.csvfileservice.dto.accountproduct;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebitAccountProductRequest {

    @NotBlank(message = "productCode is required")
    private String productCode;

    private String fundCode;

    private String fundName;

    private String imCode;

    private String imName;

    private String currency;

    private String cashAccount;

    private String bankName;
}
