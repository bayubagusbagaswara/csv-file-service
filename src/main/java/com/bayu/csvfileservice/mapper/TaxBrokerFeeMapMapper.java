package com.bayu.csvfileservice.mapper;

import com.bayu.csvfileservice.dto.taxbrokerfee.TaxBrokerFeeMapDto;
import com.bayu.csvfileservice.model.TaxBrokerFeeMap;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaxBrokerFeeMapMapper {

    TaxBrokerFeeMapDto toDto(TaxBrokerFeeMap entity);

    List<TaxBrokerFeeMapDto> toDtos(List<TaxBrokerFeeMap> entities);

}
