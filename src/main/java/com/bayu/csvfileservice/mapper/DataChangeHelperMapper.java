package com.bayu.csvfileservice.mapper;

import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.exception.JsonSerializeException;
import com.bayu.csvfileservice.util.JsonViews;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class DataChangeHelperMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> DataChangeDto toAuditDto(
            DataChangeDto baseDto,
            T before,
            T after
    ) {
        try {
            String jsonBefore = before != null
                    ? objectMapper
                    .writerWithView(JsonViews.Audit.class)
                    .writeValueAsString(before)
                    : null;

            String jsonAfter = after != null
                    ? objectMapper
                    .writerWithView(JsonViews.Audit.class)
                    .writeValueAsString(after)
                    : null;

            return baseDto.toBuilder()
                    .jsonDataBefore(jsonBefore)
                    .jsonDataAfter(jsonAfter)
                    .build();

        } catch (Exception e) {
            throw new JsonSerializeException(
                    "Failed to serialize payload: " + e.getMessage(), e
            );
        }
    }

    public <T> DataChangeDto forAdd(DataChangeDto baseDto, T after) {
        return toAuditDto(baseDto, null, after);
    }

    public <T> DataChangeDto forEdit(DataChangeDto baseDto, T before, T after) {
        return toAuditDto(baseDto, before, after);
    }

    public <T> DataChangeDto forDelete(DataChangeDto baseDto, T before) {
        return toAuditDto(baseDto, before, null);
    }

}
