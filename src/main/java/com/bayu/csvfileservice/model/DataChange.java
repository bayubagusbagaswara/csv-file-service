package com.bayu.csvfileservice.model;

import com.bayu.csvfileservice.model.base.BaseApproval;
import com.bayu.csvfileservice.model.enumerator.ChangeAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "data_change")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DataChange extends BaseApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ChangeAction action;

    private String entityName;

    private String entityId;

    @Lob
    @Column(name = "data_before", columnDefinition = "LONGTEXT")
    private String jsonDataBefore;

    @Lob
    @Column(name = "data_after", columnDefinition = "LONGTEXT")
    private String jsonDataAfter;

    private String description;

    private String httpMethod;

    private String endpoint;

    private Boolean requestBody;

    private Boolean requestParam;

    private Boolean pathVariable;

    private String menu;
}
