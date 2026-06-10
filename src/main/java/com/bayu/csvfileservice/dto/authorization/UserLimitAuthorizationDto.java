package com.bayu.csvfileservice.dto.authorization;

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
public class UserLimitAuthorizationDto {

    @JsonView({
            JsonViews.Query.class,
            JsonViews.Audit.class,
            JsonViews.AuditForEdit.class,
            JsonViews.AuditForDelete.class
    })
    private Long id;

    @JsonProperty("userId")
    @JsonView({
            JsonViews.Query.class,
            JsonViews.Audit.class,
            JsonViews.AuditForAdd.class,
            JsonViews.AuditForEdit.class,
            JsonViews.AuditForDelete.class
    })
    private String userId;

    @JsonProperty("username")
    @JsonView({
            JsonViews.Query.class,
            JsonViews.Audit.class,
            JsonViews.AuditForAdd.class,
            JsonViews.AuditForEdit.class,
            JsonViews.AuditForDelete.class
    })
    private String username;

    @JsonProperty("userGroup")
    @JsonView({
            JsonViews.Query.class,
            JsonViews.Audit.class,
            JsonViews.AuditForAdd.class,
            JsonViews.AuditForEdit.class,
            JsonViews.AuditForDelete.class
    })
    private String userGroup;

    @JsonProperty("limit")
    @JsonView({
            JsonViews.Query.class,
            JsonViews.Audit.class,
            JsonViews.AuditForAdd.class,
            JsonViews.AuditForEdit.class,
            JsonViews.AuditForDelete.class
    })
    private String limit;

}