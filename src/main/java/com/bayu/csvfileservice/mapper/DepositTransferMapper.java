package com.bayu.csvfileservice.mapper;

import com.bayu.csvfileservice.dto.sinvest.SInvestRequest;
import com.bayu.csvfileservice.model.DepositTransferMap;
import com.bayu.csvfileservice.model.SInvest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepositTransferMapper {

    SInvest toSInvest(SInvestRequest request);

    DepositTransferMap toDepositTransferMap(SInvest sInvest);
}