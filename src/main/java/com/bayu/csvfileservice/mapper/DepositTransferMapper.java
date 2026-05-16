package com.bayu.csvfileservice.mapper;

import com.bayu.csvfileservice.dto.deposittransfer.map.DepositTransferMapDto;
import com.bayu.csvfileservice.model.DepositTransferMap;
import com.bayu.csvfileservice.model.SInvest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepositTransferMapper {

    DepositTransferMap fromSInvestToDepositTransferMap(SInvest sInvest);

    DepositTransferMapDto fromEntityToDto(DepositTransferMap entity);

    List<DepositTransferMapDto> fromEntitiesToDtos(List<DepositTransferMap> entities);

}