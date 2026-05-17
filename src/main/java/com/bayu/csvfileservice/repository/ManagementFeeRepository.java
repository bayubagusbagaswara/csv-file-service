package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.ManagementFee;
import com.bayu.csvfileservice.model.enumerator.Month;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManagementFeeRepository extends JpaRepository<ManagementFee, Long> {

    void deleteByFundCodeAndMonthAndYear(
            String fundCode,
            Month month,
            Integer year
    );

    List<ManagementFee> findAllByMonthAndYear(Month month, Integer year);

    boolean existsByFundCodeAndMonthAndYear(String fundCode, Month month, Integer year);

}
