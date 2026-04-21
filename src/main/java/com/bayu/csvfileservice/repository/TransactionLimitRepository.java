package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.TransactionLimit;
import com.bayu.csvfileservice.model.enumerator.TransferMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionLimitRepository extends JpaRepository<TransactionLimit, Long> {

    Optional<TransactionLimit> findByTransferMethod(TransferMethod transferMethod);

}
