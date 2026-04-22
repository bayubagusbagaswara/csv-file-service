package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.CreateNcbsResponse;
import com.bayu.csvfileservice.model.NcbsResponse;
import com.bayu.csvfileservice.model.enumerator.MiddlewareServiceType;
import com.bayu.csvfileservice.repository.NcbsResponseRepository;
import com.bayu.csvfileservice.service.NcbsResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NcbsResponseServiceImpl implements NcbsResponseService {

    private final NcbsResponseRepository ncbsResponseRepository;

    @Override
    public NcbsResponse create(CreateNcbsResponse createNcbsResponse) {
        return null;
    }

    @Override
    public List<NcbsResponse> getAllByServiceTypeAndCurrentDate(MiddlewareServiceType serviceType, LocalDate date) {
        return List.of();
    }
}
