package com.bayu.csvfileservice.repository;

import com.bayu.csvfileservice.model.UserLimitAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLimitAuthorizationRepository extends JpaRepository<UserLimitAuthorization, Long> {

    Optional<UserLimitAuthorization> findByUserId(String userId);

    boolean existsByUserId(String userId);

}
