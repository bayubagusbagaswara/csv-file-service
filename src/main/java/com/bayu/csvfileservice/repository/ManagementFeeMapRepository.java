package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.ManagementFeeMap;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.Month;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagementFeeMapRepository extends JpaRepository<ManagementFeeMap, Long> {

    void deleteByMonthAndYear(Month month, Integer year);

    void deleteByMonthAndYearAndStatus(Month month, Integer year, MappingStatus status);

    boolean existsByReferenceCombination(String referenceCombination);

}
