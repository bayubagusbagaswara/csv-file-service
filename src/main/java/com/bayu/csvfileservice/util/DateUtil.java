package com.bayu.csvfileservice.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateUtil {


    public String format(LocalDate date, DateTimeFormatter formatter) {
        if (date == null) {
            return null;
        }

        return date.format(formatter);
    }

    public String format(LocalDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime == null) {
            return null;
        }

        return dateTime.format(formatter);
    }

    public LocalDate parseToLocalDate(String date, DateTimeFormatter formatter) {
        if (date == null || date.isBlank()) {
            return null;
        }

        return LocalDate.parse(date, formatter);
    }

    public LocalDateTime parseToLocalDateTime(String dateTime, DateTimeFormatter formatter) {
        if (dateTime == null || dateTime.isBlank()) {
            return null;
        }

        return LocalDateTime.parse(dateTime, formatter);
    }

}