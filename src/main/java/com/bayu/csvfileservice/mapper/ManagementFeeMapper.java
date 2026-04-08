package com.bayu.csvfileservice.mapper;

import com.bayu.csvfileservice.dto.managementfee.ManagementFeeDto;
import com.bayu.csvfileservice.dto.managementfee.ManagementFeeRequest;
import com.bayu.csvfileservice.model.ManagementFeeRaw;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ManagementFeeMapper {

    // tambahkan field referenceCombine
    @Mapping(
            target = "referenceCombination",
            expression = "java(createReferenceCombination(request.getFundCode(), request.getAmount()))"
    )
    ManagementFeeDto fromRequestToDto(ManagementFeeRequest request);

    ManagementFeeDto toDto(ManagementFeeRaw entity);

    ManagementFeeDto fromEntityToDto(ManagementFeeRaw entity);

    default String createReferenceCombination(String fundCode, String amount) {
        if (fundCode == null || amount == null) {
            return null;
        }
        return fundCode + amount.replace(",", "").replace(".", "");
    }
}
