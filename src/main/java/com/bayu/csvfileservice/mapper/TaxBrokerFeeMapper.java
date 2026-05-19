package com.bayu.csvfileservice.mapper;

import com.bayu.csvfileservice.dto.taxbrokerfee.TaxBrokerFeeDto;
import com.bayu.csvfileservice.dto.taxbrokerfee.TaxBrokerFeeRequest;
import com.bayu.csvfileservice.model.TaxBrokerFee;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaxBrokerFeeMapper {

    TaxBrokerFeeDto toDto(TaxBrokerFee entity);

    List<TaxBrokerFeeDto> toDtos(List<TaxBrokerFee> entities);

    TaxBrokerFeeDto fromRequestToDto(TaxBrokerFeeRequest request);

    TaxBrokerFee toEntity(TaxBrokerFeeDto dto);
}