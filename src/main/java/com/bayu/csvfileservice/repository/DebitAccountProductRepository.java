package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.DebitAccountProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DebitAccountProductRepository extends JpaRepository<DebitAccountProduct, Long> {

    Optional<DebitAccountProduct> findByFundCode(String fundCode);
}
