package com.bayu.csvfileservice.dto.managementfee;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagementFeeRequest {

    @JsonProperty(value = "mutualFundName")
    @JsonAlias({"mutualFundName"})
    @NotBlank(message = "Mutual Fund Name must not be blank")
    private String mutualFundName;

    @JsonProperty(value = "investmentManager")
    @JsonAlias({"investmentManager"})
    @NotBlank(message = "Investment Manager must not be blank")
    private String investmentManager;

    @JsonProperty(value = "fundCode")
    @JsonAlias({"fundCode"})
    @NotBlank(message = "Fund Code must not be blank")
    private String fundCode;

    @JsonProperty(value = "debitAccount")
    @JsonAlias({"debitAccount"})
    @NotBlank(message = "Debit Account must not be blank")
    private String debitAccount;

    @JsonProperty(value = "amount")
    @JsonAlias({"amount"})
    @NotBlank(message = "Amount must not be blank")
    private String amount;

    @JsonProperty(value = "creditAccount")
    @JsonAlias({"creditAmount"})
    @NotBlank(message = "Credit Account must not be blank")
    private String creditAccount;

    @JsonProperty(value = "beneficiaryName")
    @JsonAlias({"beneficiaryName"})
    @NotBlank(message = "Beneficiary Name must not be blank")
    private String beneficiaryName;

    @JsonProperty(value = "bankName")
    @JsonAlias({"bankName"})
    @NotBlank(message = "Bank Name must not be blank")
    private String bankName;

    @JsonProperty(value = "paymentInstructions")
    @JsonAlias({"paymentInstructions"})
    @NotBlank(message = "Payment Instructions must not be blank")
    private String paymentInstructions;

    @JsonProperty(value = "paymentType")
    @JsonAlias({"paymentType"})
    @NotBlank(message = "Payment Type must not be blank")
    private String paymentType;

    @JsonProperty(value = "period")
    @JsonAlias({"period"})
    @NotBlank(message = "Period must not be blank")
    private String period;

    @JsonProperty(value = "description")
    @JsonAlias({"description"})
    @NotBlank(message = "Description must not be blank")
    private String description;

    @JsonProperty(value = "bankCode")
    @JsonAlias({"bankCode"})
    @NotBlank(message = "Bank Code must not be blank")
    private String bankCode;

}
