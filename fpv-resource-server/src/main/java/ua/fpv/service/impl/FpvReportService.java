package ua.fpv.service.impl;

import ua.fpv.entity.request.FpvReportCreateRequest;
import ua.fpv.entity.request.FpvReportUpdateRequest;
import ua.fpv.entity.response.FpvReportResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FpvReportService {

    Optional<FpvReportResponse> findById(Long id);

    boolean existsById(Long id);

    List<FpvReportResponse> findAll();

    FpvReportResponse save(FpvReportCreateRequest fpvReportCreateRequest);

    FpvReportResponse update(Long id, FpvReportUpdateRequest fpvReportUpdateRequest);

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);

    Map<String, Object> getStatistics();
}
