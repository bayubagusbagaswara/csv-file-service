package com.bayu.csvfileservice.util;

import com.bayu.csvfileservice.model.CsvData;
import com.bayu.csvfileservice.model.RGDaily;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@UtilityClass
public class CsvDataMapper {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy");


    public static List<CsvData> mapCsvData(List<String[]> rows) {
        List<CsvData> csvDataList = new ArrayList<>();

        // Assuming the first row contains headers, start from index 1
        // kita ganti mulai dari 0, karena tidak ada header column
        for (String[] row : rows) {
            CsvData csvData = new CsvData();
            csvData.setName(row[0]);
            csvData.setAge(parseIntOrDefault(row[1]));

            System.out.println("Name : " + csvData.getName());
            System.out.println("Age : " + csvData.getAge());

            csvDataList.add(csvData);
        }

        return csvDataList;
    }

    public static List<RGDaily> mapCsvDataRgDaily(List<String[]> rows) {
        List<RGDaily> csvDataList = new ArrayList<>();

        for (String[] row : rows) {
            RGDaily rgDaily = new RGDaily();
            rgDaily.setNumber(parseIntOrDefault(row[0]));
            rgDaily.setDate(parseDateOrDefault(row[1], null));
            rgDaily.setCustomerCode(trimString(row[2]));
            rgDaily.setProduct(trimString(row[3]));
            rgDaily.setPrice(parseBigDecimalOrDefault(row[4], null));
            rgDaily.setPriceFormatter(row[5]);
            rgDaily.setPriceA(parseBigDecimalOrDefault(row[6], null));
            rgDaily.setPriceB(parseIntOrDefault(row[7]));

            csvDataList.add(rgDaily);
        }

        return csvDataList;
    }

    private static String trimString(String value) {
        return value != null ? value.trim() : null;
    }

    private static Integer parseIntOrDefault(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDate parseDateOrDefault(String value, LocalDate defaultValue) {
        try {
            return LocalDate.parse(value, dateFormatter);
        } catch (Exception e) {
            // Log an error or handle the exception as needed
            return defaultValue;
        }
    }

//    private static Date parseDateOrDefault1(String value, Date defaultValue) {
//        try {
//            return Date.parse(value, dateFormatter);
//        } catch (Exception e) {
//            // Log an error or handle the exception as needed
//            return defaultValue;
//        }
//    }

    private static BigDecimal parseBigDecimalOrDefault(String value, BigDecimal defaultValue) {
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            // Log an error or handle the exception as needed
            return defaultValue;
        }
    }
}
