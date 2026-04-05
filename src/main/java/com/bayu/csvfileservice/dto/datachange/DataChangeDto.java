package com.bayu.csvfileservice.dto.datachange;

import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.model.enumerator.ChangeAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//Apa Fungsi toBuilder()? untuk clone object + bisa modify sebagian field
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DataChangeDto {

    private Long id;
    private ApprovalStatus approvalStatus;
    private String inputId;
    private LocalDateTime inputDate;
    private String inputIpAddress;

    private String approveId;
    private LocalDateTime approveDate;
    private String approveIpAddress;

    private ChangeAction action;

    private String entityId;
    private String entityName;

    private String jsonDataBefore;
    private String jsonDataAfter;
    private String description;
    private String httpMethod;
    private String endpoint;
    private Boolean requestBody;
    private Boolean requestParam;
    private Boolean pathVariable;
    private String menu;
}
