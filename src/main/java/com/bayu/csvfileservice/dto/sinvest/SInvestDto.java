package com.bayu.csvfileservice.dto.sinvest;

import com.bayu.csvfileservice.util.JsonViews;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SInvestDto {

    @JsonView({JsonViews.Query.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class})
    private Long id;

    @JsonProperty("imCode")
    @JsonView({
            JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class
    })
    private String imCode;

    @JsonProperty("imName")
    @JsonView({
            JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class
    })
    private String imName;

    @JsonProperty("fundCode")
    @JsonView({
            JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class
    })
    private String fundCode;

    @JsonProperty("fundName")
    @JsonView({
            JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class
    })
    private String fundName;

    @JsonProperty("bankCode")
    @JsonView({
            JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class
    })
    private String bankCode;

    @JsonProperty("bankName")
    @JsonView({
            JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class
    })
    private String bankName;

    @JsonProperty("cashAccountName")
    @JsonView({
            JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class
    })
    private String cashAccountName;

    @JsonProperty("cashAccountNo")
    @JsonView({
            JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class
    })
    private String cashAccountNo;

    @JsonProperty("currency")
    @JsonView({
            JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class
    })
    private String currency;

    @JsonProperty("principle")
    @JsonView({
            JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class
    })
    private String principle;

    @JsonProperty("date")
    @JsonView({
            JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class
    })
    private String date;

    @JsonProperty("referenceNo")
    @JsonView({
            JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class
    })
    private String referenceNo;

    @JsonProperty("siReferenceId")
    @JsonView({
            JsonViews.Query.class, JsonViews.Audit.class,
            JsonViews.AuditForAdd.class, JsonViews.AuditForEdit.class, JsonViews.AuditForDelete.class
    })
    private String siReferenceId;

    @JsonView({JsonViews.Query.class})
    private String transferScope;

    @JsonView({JsonViews.Query.class})
    private String transferMethod;

    @JsonView({JsonViews.Query.class})
    private String mappingStatus;

    @JsonView({JsonViews.Query.class})
    private int rowNumber;

}
