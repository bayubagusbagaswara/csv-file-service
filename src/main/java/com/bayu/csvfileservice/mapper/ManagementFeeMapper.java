package com.bayu.csvfileservice.mapper;

import com.bayu.csvfileservice.dto.managementfee.ManagementFeeRequest;
import com.bayu.csvfileservice.dto.managementfee.ViewManagementFeeAfterAdd;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ManagementFeeMapper {

    ViewManagementFeeAfterAdd fromRequestToViewAfterAdd(ManagementFeeRequest request);

}
