package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.ManagementFeeRaw;
import com.bayu.csvfileservice.model.enumerator.Month;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManagementFeeRawRepository extends JpaRepository<ManagementFeeRaw, Long> {

    void deleteByFundCodeAndMonthAndYear(
            String fundCode,
            Month month,
            Integer year
    );

    List<ManagementFeeRaw> findAllByMonthAndYear(Month month, Integer year);
}
