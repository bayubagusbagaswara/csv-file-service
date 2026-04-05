package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.DataChange;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataChangeRepository extends JpaRepository<DataChange, Long> {
}
