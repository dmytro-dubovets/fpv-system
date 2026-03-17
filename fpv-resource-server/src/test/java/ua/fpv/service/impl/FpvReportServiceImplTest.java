package ua.fpv.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.fpv.entity.model.FpvDrone;
import ua.fpv.entity.model.FpvReport;
import ua.fpv.entity.request.FpvDroneRequest;
import ua.fpv.entity.request.FpvReportCreateRequest;
import ua.fpv.entity.response.FpvReportResponse;
import ua.fpv.repository.FpvReportRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FpvReportServiceTest {

    @Mock
    private FpvReportRepository fpvReportRepository;

    @Mock
    private FpvDroneServiceImpl fpvDroneService;

    @InjectMocks
    private FpvReportServiceImpl fpvReportService;

    @Test
    @DisplayName("Збереження звіту має викликати метод save у репозиторію")
    void save_ShouldCallRepositorySave() {
        FpvDroneRequest fpvDrone = new FpvDroneRequest();
        fpvDrone.setFpvCraftName("Shrike");
        fpvDrone.setFpvSerialNumber("1241414343");
        fpvDrone.setFpvModel(FpvDrone.FpvModel.KAMIKAZE);

        FpvReportCreateRequest report = new FpvReportCreateRequest();
        report.setDateTimeFlight(LocalDateTime.now());
        report.setAdditionalInfo("200");
        report.setLostFPVDueToREB(false);
        report.setOnTargetFPV(true);
        report.setCoordinatesMGRS("36UXV123");
        report.setFpvDrone(fpvDrone);

        FpvReport savedEntity = new FpvReport();
        savedEntity.setFpvReportId(1L);
        savedEntity.setCoordinatesMGRS("36UXV123");

        FpvDrone mockDrone = new FpvDrone();
        mockDrone.setFpvSerialNumber("1241414343");
        when(fpvDroneService.mapToFpvDroneEntity(any(FpvDroneRequest.class))).thenReturn(mockDrone);

        when(fpvReportRepository.save(any(FpvReport.class))).thenReturn(savedEntity);

        FpvReportResponse result = fpvReportService.save(report);

        assertNotNull(result);
        assertEquals(1L, result.getFpvReportId());
        assertEquals("36UXV123", result.getCoordinatesMGRS());
    }
}
