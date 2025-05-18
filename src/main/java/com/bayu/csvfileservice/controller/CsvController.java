package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.model.CsvData;
import com.bayu.csvfileservice.model.RGDaily;
import com.bayu.csvfileservice.service.CsvReaderService;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/csv")
@RequiredArgsConstructor
public class CsvController {

    private final CsvReaderService csvReaderService;

    @GetMapping("/read")
    public List<CsvData> readCsv() throws IOException, CsvException {
        // csv_data.csv
        String filePath = "csv_data.csv";
        return csvReaderService.readCsvData(filePath);
    }

    @GetMapping("/read/rg")
    public List<RGDaily> readCsvRg() throws IOException, CsvException {
        // csv_data.csv
        String filePath = "RGDaily.csv";
        return csvReaderService.readCsvDataRg(filePath);
    }
}
