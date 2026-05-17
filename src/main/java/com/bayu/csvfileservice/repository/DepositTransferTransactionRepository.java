package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.DepositTransferTransaction;
import com.bayu.csvfileservice.model.enumerator.ProcessType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DepositTransferTransactionRepository extends JpaRepository<DepositTransferTransaction, Long> {

    List<DepositTransferTransaction> findAllByDate(LocalDate currentDate);

    List<DepositTransferTransaction> findAllByProcessTypeAndDate(ProcessType processType, LocalDate currentDate);

}
