package com.bayu.csvfileservice.mapper;

import com.bayu.csvfileservice.dto.accountproduct.DebitAccountProductDto;
import com.bayu.csvfileservice.dto.accountproduct.DebitAccountProductRequest;
import com.bayu.csvfileservice.model.DebitAccountProduct;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DebitAccountProductMapper {

    DebitAccountProductDto fromEntityToDto(DebitAccountProduct entity);

    List<DebitAccountProductDto> fromEntitiesToDtos(List<DebitAccountProduct> entities);

    DebitAccountProductDto fromRequestToDto(DebitAccountProductRequest request);

    DebitAccountProduct toEntity(DebitAccountProductDto dto);

}