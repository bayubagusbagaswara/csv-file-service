package com.bayu.csvfileservice.mapper;

import com.bayu.csvfileservice.dto.sinvest.SInvestDto;
import com.bayu.csvfileservice.dto.sinvest.SInvestRequest;
import com.bayu.csvfileservice.model.SInvest;
import com.bayu.csvfileservice.util.DateFormatConstant;
import com.bayu.csvfileservice.util.DateUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring")
public interface SInvestMapper {

    SInvestDto fromRequestToDto(SInvestRequest sInvestRequest);

    @Mapping(target = "date", source = "date", qualifiedByName = "localDateToString")
    SInvestDto fromEntityToDto(SInvest sInvest);

    List<SInvestDto> fromEntitiesToDtoList(List<SInvest> sInvests);

    @Named("localDateToString")
    default String localDateToString(LocalDate localDate) {
        return DateUtil.format(localDate, DateFormatConstant.YYYYMMDD);
    }

}
