package com.bayu.csvfileservice.dto.managementfee;

import com.bayu.csvfileservice.util.JsonViews;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ManagementFeeDto {

    @JsonView(JsonViews.Query.class)  // Hanya tampil di Query
    private Long id;

    @JsonView(JsonViews.Query.class)  // Hanya tampil di Query
    private String month;

    @JsonView(JsonViews.Query.class)  // Hanya tampil di Query
    private Integer year;

    @JsonView({JsonViews.Audit.class, JsonViews.Query.class})  // Tampil di kedua view
    @JsonProperty("namaReksadana")
    private String mutualFundName;

    @JsonView({JsonViews.Audit.class, JsonViews.Query.class})
    private String investmentManager;

    @JsonView({JsonViews.Audit.class, JsonViews.Query.class})
    private String fundCode;

    @JsonView({JsonViews.Audit.class, JsonViews.Query.class})
    private String debitAccount;

    @JsonView({JsonViews.Audit.class, JsonViews.Query.class})
    private String amount;

    @JsonView({JsonViews.Audit.class, JsonViews.Query.class})
    private String creditAccount;

    @JsonView({JsonViews.Audit.class, JsonViews.Query.class})
    private String beneficiaryName;

    @JsonView({JsonViews.Audit.class, JsonViews.Query.class})
    private String bankName;

    @JsonView({JsonViews.Audit.class, JsonViews.Query.class})
    private String paymentInstructions;

    @JsonView({JsonViews.Audit.class, JsonViews.Query.class})
    private String paymentType;

    @JsonView({JsonViews.Audit.class, JsonViews.Query.class})
    private String period;

    @JsonView({JsonViews.Audit.class, JsonViews.Query.class})
    private String description;

    @JsonView({JsonViews.Audit.class, JsonViews.Query.class})
    private String bankCode;

    @JsonView({JsonViews.Audit.class, JsonViews.Query.class})
    private String referenceCombination;
}
