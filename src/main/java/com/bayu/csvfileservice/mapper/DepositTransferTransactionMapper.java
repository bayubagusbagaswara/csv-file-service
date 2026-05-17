package com.bayu.csvfileservice.mapper;

import com.bayu.csvfileservice.dto.deposittransfer.transaction.DepositTransferTransactionDto;
import com.bayu.csvfileservice.model.DepositTransferTransaction;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepositTransferTransactionMapper {

    DepositTransferTransactionDto fromEntityToDto(DepositTransferTransaction entity);

    List<DepositTransferTransactionDto> fromEntitiesToDtos(List<DepositTransferTransaction> entities);

}
