package com.bayu.csvfileservice.mapper;

import com.bayu.csvfileservice.dto.managementfee.ManagementFeeDto;
import com.bayu.csvfileservice.dto.managementfee.ManagementFeeRequest;
import com.bayu.csvfileservice.dto.managementfee.ViewManagementFeeAfterAdd;
import com.bayu.csvfileservice.dto.managementfee.ViewManagementFeeBeforeDelete;
import com.bayu.csvfileservice.model.ManagementFeeRaw;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ManagementFeeMapper {

    ViewManagementFeeAfterAdd fromRequestToViewAfterAdd(ManagementFeeRequest request);

    ManagementFeeDto toDto(ManagementFeeRaw managementFeeRaw);

    ViewManagementFeeBeforeDelete fromRequestToViewBeforeDelete(ManagementFeeDto dto);

}
