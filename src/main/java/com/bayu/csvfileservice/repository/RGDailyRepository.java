package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.RGDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RGDailyRepository extends JpaRepository<RGDaily, Long> {
}
