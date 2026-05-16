package com.bayu.csvfileservice.repository;


import com.bayu.csvfileservice.model.SInvest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface SInvestRepository extends JpaRepository<SInvest, Long> {

    boolean existsBySiReferenceId(String siReferenceId);

    boolean existsBySiReferenceIdAndDate(String siReferenceId, LocalDate date);

}