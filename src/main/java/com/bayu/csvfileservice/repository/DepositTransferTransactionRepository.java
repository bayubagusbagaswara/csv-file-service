package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.DepositTransferTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositTransferTransactionRepository extends JpaRepository<DepositTransferTransaction, Long> {
}
