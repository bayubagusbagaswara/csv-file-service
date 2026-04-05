package com.bayu.csvfileservice.service.impl;

import com.bayu.csvfileservice.dto.ErrorDetail;
import com.bayu.csvfileservice.dto.ProcessResult;
import com.bayu.csvfileservice.model.*;
import com.bayu.csvfileservice.model.enumerator.MappingStatus;
import com.bayu.csvfileservice.model.enumerator.Month;
import com.bayu.csvfileservice.repository.ManagementFeeMapRepository;
import com.bayu.csvfileservice.repository.ManagementFeeRawRepository;
import com.bayu.csvfileservice.repository.MasterBankRepository;
import com.bayu.csvfileservice.service.ManagementFeeMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManagementFeeMapServiceImpl implements ManagementFeeMapService {

    private final ManagementFeeRawRepository rawRepository;
    private final ManagementFeeMapRepository mapRepository;
    private final MasterBankRepository masterBankRepository;

    @Override
    @Transactional
    public ProcessResult map(Month month, Integer year) {

        ProcessResult result = new ProcessResult();

        // 🔥 Replace strategy
        mapRepository.deleteByMonthAndYear(month, year);

        List<ManagementFeeRaw> raws =
                rawRepository.findAllByMonthAndYear(month, year);

        if (raws.isEmpty()) {
            log.warn("No data found for month {} year {}", month, year);
            return result;
        }

        for (ManagementFeeRaw raw : raws) {

            try {

                MasterBank bank = masterBankRepository
                        .findByBankCode(raw.getBankCode())
                        .orElseThrow(() ->
                                new IllegalArgumentException("Bank not found: " + raw.getBankCode())
                        );

                ManagementFeeMap map = ManagementFeeMap.builder()
                        .fundCode(raw.getFundCode())
                        .month(raw.getMonth())
                        .year(raw.getYear())
                        .amount(raw.getAmount())
                        .bankCode(bank.getBankCode())
                        .status(MappingStatus.DRAFT)
                        .build();

                mapRepository.save(map);
                result.addSuccess();

            } catch (Exception e) {

                log.error("Mapping failed fundCode {}", raw.getFundCode(), e);

                result.addError(
                        ErrorDetail.of(
                                "fundCode",
                                raw.getFundCode(),
                                List.of(e.getMessage())
                        )
                );
            }
        }

        return result;
    }

    // ======================= APPROVE =======================

    @Transactional
    public ProcessResult approve(List<Long> idList) {

        ProcessResult result = new ProcessResult();

        List<ManagementFeeMap> list = mapRepository.findAllById(idList);

        for (ManagementFeeMap item : list) {

            if (item.getStatus() != MappingStatus.DRAFT) {

                result.addError(
                        ErrorDetail.of(
                                "id",
                                String.valueOf(item.getId()),
                                List.of("Only DRAFT data can be approved")
                        )
                );
                continue;
            }

            item.setStatus(MappingStatus.READY);
            mapRepository.save(item);

            result.addSuccess();
        }

        return result;
    }

    // ======================= SEND =======================

    @Transactional
    public ProcessResult sendToMiddleware(List<Long> idList) {

        ProcessResult result = new ProcessResult();

        List<ManagementFeeMap> list = mapRepository.findAllById(idList);

        for (ManagementFeeMap item : list) {

            try {

                validateSend(item);

                // ================= CREATE REQUEST =================
                NcbsRequest request = ncbsRequestService.createRequest(item);

                item.setStatus(MappingStatus.SENT);
                item.setReferenceId(request.getReferenceId());
                mapRepository.save(item);

                // ================= CALL MIDDLEWARE =================
                NcbsResponse response = ncbsResponseService.callMiddleware(request);

                // ================= HANDLE RESPONSE =================
                updateStatusFromResponse(item, response);

                result.addSuccess();

            } catch (Exception e) {

                log.error("Send failed id {}", item.getId(), e);

                item.setStatus(MappingStatus.FAILED);
                mapRepository.save(item);

                result.addError(
                        ErrorDetail.of(
                                "id",
                                String.valueOf(item.getId()),
                                List.of("Send failed")
                        )
                );
            }
        }

        return result;
    }

    // ======================= HELPER =======================

    private void validateSend(ManagementFeeMap item) {

        if (!List.of(MappingStatus.READY, MappingStatus.RETRY)
                .contains(item.getStatus())) {

            throw new IllegalStateException(
                    "Data with id " + item.getId() + " cannot be sent"
            );
        }
    }

    private void updateStatusFromResponse(
            ManagementFeeMap item,
            NcbsResponse response
    ) {

        if ("00".equals(response.getResponseCode())) {

            item.setStatus(MappingStatus.SUCCESS);

        } else if ("SALDO_KURANG".equalsIgnoreCase(response.getStatusCode())) {

            item.setStatus(MappingStatus.RETRY);

        } else {

            item.setStatus(MappingStatus.FAILED);
        }

        mapRepository.save(item);
    }
}
