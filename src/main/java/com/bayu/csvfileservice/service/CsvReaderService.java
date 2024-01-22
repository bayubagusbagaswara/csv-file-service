package com.bayu.csvfileservice.service;

import com.bayu.csvfileservice.model.CsvData;
import com.bayu.csvfileservice.model.RGDaily;
import com.bayu.csvfileservice.repository.CsvReaderRepository;
import com.bayu.csvfileservice.repository.RGDailyRepository;
import com.bayu.csvfileservice.util.CsvDataMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvReaderService {

    private final CsvReaderRepository csvReaderRepository;
    private final RGDailyRepository rgDailyRepository;

    public List<CsvData> readCsvData(String filePath) throws IOException, CsvException {
        // harusnya ada pengecekan dulu apakah file tersebut sudah di save di database atau belum
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> rows = reader.readAll();

            // Convert rows to a list of CsvData objects
            // Assuming the first row contains headers
            List<CsvData> csvDataList = CsvDataMapper.mapCsvData(rows);

            // save csv data list to table
            return csvReaderRepository.saveAll(csvDataList);
        }
    }

    public List<RGDaily> readCsvDataRg(String filePath) throws IOException, CsvException {
        // harusnya ada pengecekan dulu apakah file tersebut sudah di save di database atau belum
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> rows = reader.readAll();

            // Convert rows to a list of CsvData objects
            // Assuming the first row contains headers
            List<RGDaily> rgDailies = CsvDataMapper.mapCsvDataRgDaily(rows);

            // save csv data list to table

            return rgDailyRepository.saveAll(rgDailies);
        }
    }
}
