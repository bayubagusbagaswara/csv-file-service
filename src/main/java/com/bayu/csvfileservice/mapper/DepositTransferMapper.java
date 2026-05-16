package com.bayu.csvfileservice.mapper;

import com.bayu.csvfileservice.model.DepositTransferMap;
import com.bayu.csvfileservice.model.SInvest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepositTransferMapper {

    DepositTransferMap fromSInvestToDepositTransferMap(SInvest sInvest);

}