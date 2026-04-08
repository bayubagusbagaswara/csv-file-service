package com.bayu.csvfileservice.mapper;

import com.bayu.csvfileservice.dto.datachange.DataChangeDto;
import com.bayu.csvfileservice.exception.JsonSerializeException;
import com.bayu.csvfileservice.util.JsonViews;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class DataChangeHelperMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Untuk ADD operation
     * jsonBefore = null
     * jsonAfter = TIDAK mengandung id, month, year (hanya field dengan AuditForAdd)
     */
    public <T> DataChangeDto forAdd(DataChangeDto baseDto, T after) {
        try {
            String jsonAfter = serializeWithView(after, JsonViews.AuditForAdd.class);

            return baseDto.toBuilder()
                    .jsonDataBefore(null)
                    .jsonDataAfter(jsonAfter)
                    .build();
        } catch (Exception e) {
            throw new JsonSerializeException(
                    "Failed to serialize forAdd: " + e.getMessage(), e
            );
        }
    }

    /**
     * Untuk EDIT operation
     * jsonBefore = mengandung id, month, year (menggunakan AuditForEdit)
     * jsonAfter = mengandung id, month, year (menggunakan AuditForEdit)
     */
    public <T> DataChangeDto forEdit(DataChangeDto baseDto, T before, T after) {
        try {
            String jsonBefore = serializeWithView(before, JsonViews.AuditForEdit.class);
            String jsonAfter = serializeWithView(after, JsonViews.AuditForEdit.class);

            return baseDto.toBuilder()
                    .jsonDataBefore(jsonBefore)
                    .jsonDataAfter(jsonAfter)
                    .build();
        } catch (Exception e) {
            throw new JsonSerializeException(
                    "Failed to serialize forEdit: " + e.getMessage(), e
            );
        }
    }

    /**
     * Untuk DELETE operation
     * jsonBefore = mengandung id, month, year (menggunakan AuditForDelete)
     * jsonAfter = null
     */
    public <T> DataChangeDto forDelete(DataChangeDto baseDto, T before) {
        try {
            String jsonBefore = serializeWithView(before, JsonViews.AuditForDelete.class);

            return baseDto.toBuilder()
                    .jsonDataBefore(jsonBefore)
                    .jsonDataAfter(null)
                    .build();
        } catch (Exception e) {
            throw new JsonSerializeException(
                    "Failed to serialize forDelete: " + e.getMessage(), e
            );
        }
    }

    /**
     * Generic method untuk serialize dengan view tertentu
     */
    private <T> String serializeWithView(T object, Class<?> viewClass) throws Exception {
        if (object == null) {
            return null;
        }
        return objectMapper
                .writerWithView(viewClass)
                .writeValueAsString(object);
    }

}
