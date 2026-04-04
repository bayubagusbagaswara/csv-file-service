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
        name = "ncbs_response",
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
public class NcbsResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdDate;

    @Column(name = "reference_id", unique = true, nullable = false, length = 50)
    private String referenceId;

    private Long entityId;

    @Enumerated(EnumType.STRING)
    private MiddlewareService service;

    @Enumerated(EnumType.STRING)
    private FeatureType featureType;

    @Enumerated(EnumType.STRING)
    private TransferScope transferScope;

    @Enumerated(EnumType.STRING)
    private TransferMethod transferMethod;

    @Enumerated(EnumType.STRING)
    private ProcessType processType;

    @Lob
    @Column(name = "json_response", columnDefinition = "LONGTEXT")
    private String jsonResponse;

    private String providerSystem;

    private String responseCode;
    private String responseMessage;

    private String statusCode;
    private String statusMessage;

    private String ncbsStatus;

}
