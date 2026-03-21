package ua.fpv.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.fpv.entity.model.FlightResult;
import ua.fpv.entity.model.FpvDrone;
import ua.fpv.entity.model.FpvPilot;
import ua.fpv.entity.model.FpvReport;
import ua.fpv.entity.request.FpvDroneRequest;
import ua.fpv.entity.request.FpvReportCreateRequest;
import ua.fpv.entity.response.FpvReportResponse;
import ua.fpv.repository.FpvPilotRepository;
import ua.fpv.repository.FpvReportRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FpvReportServiceTest {

    @Mock
    private FpvReportRepository fpvReportRepository;

    @Mock
    private FpvPilotRepository fpvPilotRepository; // Додано мок для пілотів

    @Mock
    private FpvDroneServiceImpl fpvDroneService;

    @InjectMocks
    private FpvReportServiceImpl fpvReportService;

    @Test
    @DisplayName("Збереження звіту має викликати метод save у репозиторію з правильним FlightResult")
    void save_ShouldCallRepositorySave() {
        // 1. Підготовка вхідних даних (Request)
        FpvDroneRequest droneRequest = FpvDroneRequest.builder()
                .fpvSerialNumber("1241414343")
                .fpvCraftName("Shrike")
                .fpvModel(FpvDrone.FpvModel.KAMIKAZE)
                .build();

        FpvReportCreateRequest reportRequest = FpvReportCreateRequest.builder()
                .dateTimeFlight(LocalDateTime.now())
                .additionalInfo("Ціль уражена")
                .isLostFPVDueToREB(false)
                .flightResult(FlightResult.HIT) // ОНОВЛЕНО: використовуємо Enum
                .coordinatesMGRS("36UXV123")
                .fpvDrone(droneRequest)
                .createdByUsername("123456789") // Симулюємо chatId пілота
                .build();

        // 2. Підготовка моків (Behavior)
        FpvDrone mockDroneEntity = new FpvDrone();
        mockDroneEntity.setFpvSerialNumber("1241414343");
        when(fpvDroneService.mapToFpvDroneEntity(any(FpvDroneRequest.class))).thenReturn(mockDroneEntity);

        // Мокаємо пошук пілота (повертаємо порожній Optional, щоб спрацювала логіка реєстрації, або готового пілота)
        FpvPilot mockPilot = FpvPilot.builder().username("123456789").build();
        when(fpvPilotRepository.findByUsername("123456789")).thenReturn(Optional.of(mockPilot));

        FpvReport savedEntity = FpvReport.builder()
                .fpvReportId(1L)
                .flightResult(FlightResult.HIT)
                .coordinatesMGRS("36UXV123")
                .build();

        when(fpvReportRepository.save(any(FpvReport.class))).thenReturn(savedEntity);

        // 3. Виконання методу
        FpvReportResponse result = fpvReportService.save(reportRequest);

        // 4. Перевірки (Assertions)
        assertNotNull(result);
        assertEquals(1L, result.getFpvReportId());
        assertEquals(FlightResult.HIT, result.getFlightResult()); // Перевірка Enum
        assertEquals("36UXV123", result.getCoordinatesMGRS());

        verify(fpvReportRepository, times(1)).save(any(FpvReport.class));
        verify(fpvPilotRepository, atLeastOnce()).findByUsername("123456789");
    }

    @Test
    @DisplayName("Метод статистики має повертати коректні дані")
    void getStatistics_ShouldReturnCorrectCounts() {
        when(fpvReportRepository.count()).thenReturn(10L);
        when(fpvReportRepository.countByFlightResult(FlightResult.HIT)).thenReturn(7L);
        when(fpvReportRepository.countByFlightResult(FlightResult.FIBER_CUT)).thenReturn(1L);

        var stats = fpvReportService.getStatistics();

        assertEquals(10L, stats.get("total"));
        assertEquals(7L, stats.get("hits"));
        assertEquals(1L, stats.get("fiberCuts"));
    }
}