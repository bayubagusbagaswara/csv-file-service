package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.ResponseCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResponseCodeRepository extends JpaRepository<ResponseCode, Long> {

    List<ResponseCode> findAllByName(String name);

    List<String> findCodesByName(String insufficientBalance);

}
