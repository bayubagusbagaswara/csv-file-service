package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.MasterBank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterBankRepository extends JpaRepository<MasterBank, Long> {

    Optional<MasterBank> findByBankCode(String bankCode);
}
