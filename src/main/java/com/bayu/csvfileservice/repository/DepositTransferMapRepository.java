package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.DepositTransferMap;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.TransactionStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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


    @Modifying
    @Transactional
    @Query("DELETE FROM DepositTransferMap d " +
            "WHERE d.siReferenceId = :siReferenceId " +
            "AND d.mappingStatus IN :statuses")
    void deleteBySiReferenceIdAndMappingStatusIn(
            @Param("siReferenceId") String siReferenceId,
            @Param("statuses") List<MappingStatus> statuses
    );

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END " +
            "FROM DepositTransferMap d " +
            "JOIN d.transaction t " +
            "WHERE d.siReferenceId = :siReferenceId " +
            "AND t.transactionStatus IN :statuses")
    boolean existsActiveTransactionBySiReferenceId(
            @Param("siReferenceId") String siReferenceId,
            @Param("statuses") List<TransactionStatus> statuses
    );

}