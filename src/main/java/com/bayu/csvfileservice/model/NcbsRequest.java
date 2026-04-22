package com.bayu.csvfileservice.model;

import com.bayu.csvfileservice.model.enumerator.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ncbs_request",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ncbs_request_reference_id",
                        columnNames = "reference_id"
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NcbsRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "reference_id", nullable = false, unique = true, length = 50)
    private String referenceId; // auto generate reference id uuid

    @Column(name = "entity_id")
    private Long entityId; // relasi ke entity lain

    @Lob
    @Column(name = "json_request", columnDefinition = "LONGTEXT")
    private String jsonRequest;

    @Enumerated(EnumType.STRING)
    private MiddlewareServiceType service;

    @Enumerated(EnumType.STRING)
    private FeatureType featureType;

    @Enumerated(EnumType.STRING)
    private TransferScope transferScope;

    @Enumerated(EnumType.STRING)
    private TransferMethod transferMethod;

    @Enumerated(EnumType.STRING)
    private ProcessType processType;

    // ============= OLD FIELD ================
    private String payUserRefNo;

    private String siReferenceId;

}
