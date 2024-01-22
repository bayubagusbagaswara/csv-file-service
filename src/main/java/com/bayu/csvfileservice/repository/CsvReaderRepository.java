package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.CsvData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CsvReaderRepository extends JpaRepository<CsvData, Long> {
}
