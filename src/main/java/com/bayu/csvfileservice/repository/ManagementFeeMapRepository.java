package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.ManagementFeeMap;
import com.bayu.csvfileservice.model.enumerator.Month;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagementFeeMapRepository extends JpaRepository<ManagementFeeMap, Long> {

    void deleteByMonthAndYear(Month month, Integer year);

}
