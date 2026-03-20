package ua.fpv.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.fpv.entity.model.FpvReport;
import ua.fpv.entity.request.FpvReportCreateRequest;
import ua.fpv.entity.request.FpvReportIds;
import ua.fpv.entity.request.FpvReportUpdateRequest;
import ua.fpv.entity.response.FpvReportResponse;
import ua.fpv.repository.fpvserialnumber.UniqueFpvSerialNumber;
import ua.fpv.service.impl.ExcelExportService;
import ua.fpv.service.impl.FpvReportServiceImpl;
import ua.fpv.util.AppError;
import ua.fpv.util.FpvReportNotFoundException;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/fpvreports")
public class FpvReportRestController {

    private final FpvReportServiceImpl fpvReportServiceImpl;

    private final ExcelExportService excelExportService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_fpvreport:read')")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        FpvReportResponse fpvReportResponse = fpvReportServiceImpl.findById(id)
                .orElseThrow(() -> new FpvReportNotFoundException("FPV Report with id - " + id + " is not found!"));
        return ResponseEntity.ok(fpvReportResponse);

    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_fpvreport:read')")
    public ResponseEntity<?> findAll() {
        List<FpvReportResponse> fpvReportsResponse = fpvReportServiceImpl.findAll();
        if (fpvReportsResponse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(new AppError(HttpStatus.OK.value(), "No FPVReports are available!"));
        }
        return ResponseEntity.ok(fpvReportsResponse);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_fpvreport:write')")
    public ResponseEntity<FpvReportResponse> save(@Valid @RequestBody @UniqueFpvSerialNumber FpvReportCreateRequest fpvReportCreateRequest) {
        FpvReportResponse fpvReportResponse = fpvReportServiceImpl.save(fpvReportCreateRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .buildAndExpand()
                .toUri();

        return ResponseEntity.created(location).body(fpvReportResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_fpvreport:write')")
    ResponseEntity<?> update(@Valid @RequestBody FpvReportUpdateRequest fpvReportUpdateRequest, @PathVariable Long id) {
        if (!fpvReportServiceImpl.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AppError(HttpStatus.NOT_FOUND.value(), "FPV Report with id - " + id + " is not found!"));
        }
        FpvReportResponse updatedFPVReportResponse = fpvReportServiceImpl.update(id, fpvReportUpdateRequest);
        return ResponseEntity.ok(updatedFPVReportResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_fpvreport:write')")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        fpvReportServiceImpl.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body(new AppError(HttpStatus.OK.value(), "FPVReport with ID: " + id + " is successfully deleted!"));
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('SCOPE_fpvreport:write')")
    public ResponseEntity<?> deleteAllByIds(@Valid @RequestBody FpvReportIds fpvReportIds) {
        List<Long> ids = fpvReportIds.getFpvReportIds();
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AppError(HttpStatus.BAD_REQUEST.value(), "No FPVReport IDs provided!"));
        }
        fpvReportServiceImpl.deleteAllByIds(ids);
        return ResponseEntity.status(HttpStatus.OK).body(new AppError(HttpStatus.OK.value(), "FPVReports with IDs: " + ids + " are successfully deleted!"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('SCOPE_fpvreport:read')")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("Отримання статистики звітів FPV");
        Map<String, Object> stats = fpvReportServiceImpl.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAuthority('SCOPE_fpvreport:read')")
    public ResponseEntity<byte[]> exportToExcel() throws IOException {
        List<FpvReportResponse> reports = fpvReportServiceImpl.findAll();
        byte[] excelContent = excelExportService.exportReportsToExcel(reports);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reports.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelContent);
    }

}
