package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.TaxBrokerFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaxBrokerFeeRepository extends JpaRepository<TaxBrokerFee, Long> {

    List<TaxBrokerFee> findAllByDate(LocalDate date);

}
