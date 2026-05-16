package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.DepositTransferMap;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DepositTransferMapRepository extends JpaRepository<DepositTransferMap, Long> {

    boolean existsBySiReferenceIdAndMappingStatusIn(
            String siReferenceId,
            List<MappingStatus> statuses
    );

    List<DepositTransferMap> findAllByBulkReferenceId(String bulkReferenceId);

    boolean existsBySiReferenceIdAndDate(String siReferenceId, LocalDate date);

    boolean existsBySiReferenceId(String siReferenceId);

}