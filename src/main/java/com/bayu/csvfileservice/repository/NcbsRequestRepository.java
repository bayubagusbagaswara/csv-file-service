package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.NcbsRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NcbsRequestRepository extends JpaRepository<NcbsRequest, Long> {
}
