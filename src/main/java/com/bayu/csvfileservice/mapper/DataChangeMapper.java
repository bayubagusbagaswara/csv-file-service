package com.bayu.csvfileservice.mapper;

import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.model.DataChange;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DataChangeMapper {

    DataChangeDto toDto(DataChange dataChange);

    DataChange toEntity(DataChangeDto dto);

}
