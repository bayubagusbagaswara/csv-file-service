package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.TaxBrokerFeeMap;
import com.bayu.csvfileservice.model.enumerator.ApprovalStatus;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDate;
import java.util.List;

public interface TaxBrokerFeeMapRepository extends JpaRepository<TaxBrokerFeeMap, Long> {

    boolean existsByReferenceCombinationAndStatusIn(
            String referenceCombination,
            List<MappingStatus> statuses
    );

    @Modifying
    @Transactional
    void deleteByReferenceCombinationAndStatus(
            String referenceCombination,
            MappingStatus status
    );

    List<TaxBrokerFeeMap> findAllByDate(LocalDate date);

    List<TaxBrokerFeeMap> findAllByDateAndStatus(
            LocalDate date,
            MappingStatus status
    );

    List<TaxBrokerFeeMap> findAllByDateAndApprovalStatus(
            LocalDate date,
            ApprovalStatus approvalStatus
    );

}
