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

    // Hanya tampil di Query, Edit, dan Delete (TIDAK tampil di Add)
    @JsonView({JsonViews.Query.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private Long id;

    // Hanya tampil di Query, Edit, dan Delete (TIDAK tampil di Add)
    @JsonView({JsonViews.Query.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private String month;

    // Hanya tampil di Query, Edit, dan Delete (TIDAK tampil di Add)
    @JsonView({JsonViews.Query.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private Integer year;

    // Tampil di semua view
    @JsonView({JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    @JsonProperty("namaReksadana")
    private String mutualFundName;

    // Tampil di semua view
    @JsonView({JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private String investmentManager;

    // Tampil di semua view
    @JsonView({JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private String fundCode;

    // Tampil di semua view
    @JsonView({JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private String debitAccount;

    // Tampil di semua view
    @JsonView({JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private String amount;

    // Tampil di semua view
    @JsonView({JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private String creditAccount;

    // Tampil di semua view
    @JsonView({JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private String beneficiaryName;

    // Tampil di semua view
    @JsonView({JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private String bankName;

    // Tampil di semua view
    @JsonView({JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private String paymentInstructions;

    // Tampil di semua view
    @JsonView({JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private String paymentType;

    // Tampil di semua view
    @JsonView({JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private String period;

    // Tampil di semua view
    @JsonView({JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private String description;

    // Tampil di semua view
    @JsonView({JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private String bankCode;

    // Tampil di semua view
    @JsonView({JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private String referenceCombination;

}
