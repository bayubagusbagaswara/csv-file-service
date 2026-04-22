package com.bayu.csvfileservice.mapper;

import com.bayu.csvfileservice.dto.managementfee.ManagementFeeDto;
import com.bayu.csvfileservice.dto.managementfee.ManagementFeeRequest;
import com.bayu.csvfileservice.model.ManagementFeeRaw;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ManagementFeeMapper {

    // tambahkan field referenceCombine
    @Mapping(
            target = "referenceCombination",
            expression = "java(createReferenceCombination(request.getFundCode(), request.getAmount()))"
    )
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "month", ignore = true)
//    @Mapping(target = "year", ignore = true)
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
