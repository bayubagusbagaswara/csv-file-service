package com.bayu.csvfileservice.repository;


import com.bayu.csvfileservice.model.SInvest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SInvestRepository extends JpaRepository<SInvest, Long> {

    boolean existsBySiReferenceId(String siReferenceId);
}