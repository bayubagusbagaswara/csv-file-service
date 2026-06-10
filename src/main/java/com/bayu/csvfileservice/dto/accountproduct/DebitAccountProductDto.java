package com.bayu.csvfileservice.dto.accountproduct;

import com.bayu.csvfileservice.util.JsonViews;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitAccountProductDto {

    @JsonView({
            JsonViews.Query.class,
            JsonViews.Audit.class,
            JsonViews.AuditForEdit.class,
            JsonViews.AuditForDelete.class
    })
    private Long id;

    @JsonProperty("productCode")
    @JsonView({
            JsonViews.Query.class,
            JsonViews.Audit.class,
            JsonViews.AuditForAdd.class,
            JsonViews.AuditForEdit.class,
            JsonViews.AuditForDelete.class
    })
    private String productCode;

    @JsonProperty("fundCode")
    @JsonView({
            JsonViews.Query.class,
            JsonViews.Audit.class,
            JsonViews.AuditForAdd.class,
            JsonViews.AuditForEdit.class,
            JsonViews.AuditForDelete.class
    })
    private String fundCode;

    @JsonProperty("fundName")
    @JsonView({
            JsonViews.Query.class,
            JsonViews.Audit.class,
            JsonViews.AuditForAdd.class,
            JsonViews.AuditForEdit.class,
            JsonViews.AuditForDelete.class
    })
    private String fundName;

    @JsonProperty("imCode")
    @JsonView({
            JsonViews.Query.class,
            JsonViews.Audit.class,
            JsonViews.AuditForAdd.class,
            JsonViews.AuditForEdit.class,
            JsonViews.AuditForDelete.class
    })
    private String imCode;

    @JsonProperty("imName")
    @JsonView({
            JsonViews.Query.class,
            JsonViews.Audit.class,
            JsonViews.AuditForAdd.class,
            JsonViews.AuditForEdit.class,
            JsonViews.AuditForDelete.class
    })
    private String imName;

    @JsonProperty("currency")
    @JsonView({
            JsonViews.Query.class,
            JsonViews.Audit.class,
            JsonViews.AuditForAdd.class,
            JsonViews.AuditForEdit.class,
            JsonViews.AuditForDelete.class
    })
    private String currency;

    @JsonProperty("cashAccount")
    @JsonView({
            JsonViews.Query.class,
            JsonViews.Audit.class,
            JsonViews.AuditForAdd.class,
            JsonViews.AuditForEdit.class,
            JsonViews.AuditForDelete.class
    })
    private String cashAccount;

    @JsonProperty("bankName")
    @JsonView({
            JsonViews.Query.class,
            JsonViews.Audit.class,
            JsonViews.AuditForAdd.class,
            JsonViews.AuditForEdit.class,
            JsonViews.AuditForDelete.class
    })
    private String bankName;
}
