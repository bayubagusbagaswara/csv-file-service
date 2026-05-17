package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.ManagementFeeMap;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.Month;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManagementFeeMapRepository extends JpaRepository<ManagementFeeMap, Long> {

    void deleteByMonthAndYear(Month month, Integer year);

    void deleteByMonthAndYearAndStatus(Month month, Integer year, MappingStatus status);

    boolean existsByReferenceCombination(String referenceCombination);

    void deleteByMonthAndYearAndStatusIn(Month month, Integer year, List<MappingStatus> mappingStatuses);

    boolean existsByMonthAndYearAndStatusIn(Month month, Integer year, List<MappingStatus> list);

    boolean existsByReferenceCombinationAndStatusIn(String referenceCombination, List<MappingStatus> list);

    void deleteByReferenceCombinationAndStatus(String referenceCombination, MappingStatus mappingStatus);

}
