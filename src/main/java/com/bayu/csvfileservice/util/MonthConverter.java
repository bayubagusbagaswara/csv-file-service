package com.bayu.csvfileservice.util;

import com.bayu.csvfileservice.model.enumerator.Month;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MonthConverter implements AttributeConverter<Month, String> {

    @Override
    public String convertToDatabaseColumn(Month month) {
        return month != null ? month.getLabel() : null;
    }

    @Override
    public Month convertToEntityAttribute(String dbData) {
        return dbData != null ? Month.fromLabel(dbData) : null;
    }

}
