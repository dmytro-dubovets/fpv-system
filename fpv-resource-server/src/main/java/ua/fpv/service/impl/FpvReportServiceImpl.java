package ua.fpv.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.fpv.entity.model.FpvPilot;
import ua.fpv.entity.model.FpvReport;
import ua.fpv.entity.request.FpvReportCreateRequest;
import ua.fpv.entity.request.FpvReportUpdateRequest;
import ua.fpv.entity.response.FpvReportResponse;
import ua.fpv.repository.FpvPilotRepository;
import ua.fpv.repository.FpvReportRepository;
import ua.fpv.util.FpvReportNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FpvReportServiceImpl implements FpvReportService {

    private final FpvReportRepository fpvReportRepository;

    private final FpvPilotRepository fpvPilotRepository;

    private final FpvDroneServiceImpl fpvDroneService;

    @Transactional(readOnly = true)
    public Optional<FpvReportResponse> findById(Long id) {
        String currentUsername = getCurrentUsername();

        return fpvReportRepository
                .findByFpvReportIdAndCreatedByUsername(id, currentUsername)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return fpvReportRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public List<FpvReportResponse> findAll() {
        List<FpvReport> fpvReports = fpvReportRepository.findAll();

        return fpvReports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FpvReportResponse save(FpvReportCreateRequest fpvReportCreateRequest) {
        FpvReport fpvReport = mapToEntity(fpvReportCreateRequest);
        FpvReport savedFpvReport = fpvReportRepository.save(fpvReport);
        return mapToResponse(savedFpvReport);
    }

    public FpvReportResponse update(Long id, FpvReportUpdateRequest request) {
        String currentUsername = getCurrentUsername();

        FpvReport existingReport = fpvReportRepository
                .findByFpvReportIdAndCreatedByUsername(id, currentUsername)
                .orElseThrow(() -> new FpvReportNotFoundException("FPV Report with id - " + id + " is not found!"));

        existingReport.setFpvDrone(fpvDroneService.mapToFpvDroneEntity(request.getFpvDrone()));
        existingReport.setDateTimeFlight(updateDateTimeFlight(request.getDateTimeFlight()));
        existingReport.setLostFPVDueToREB(request.isLostFPVDueToREB());
        existingReport.setOnTargetFPV(request.isOnTargetFPV());
        existingReport.setCoordinatesMGRS(request.getCoordinatesMGRS());
        existingReport.setAdditionalInfo(request.getAdditionalInfo());

        return mapToResponse(fpvReportRepository.save(existingReport));
    }

    public void deleteById(Long id) {
        String currentUsername = getCurrentUsername();

        FpvReport report = fpvReportRepository
                .findByFpvReportIdAndCreatedByUsername(id, currentUsername)
                .orElseThrow(() -> new AccessDeniedException("У вас немає прав на видалення цього звіту"));

        fpvReportRepository.delete(report);
    }

    public void deleteAllByIds(List<Long> ids) {
        String currentUsername = getCurrentUsername();

        List<FpvReport> reportsToDelete = fpvReportRepository.findAllByCreatedByUsername(currentUsername)
                .stream()
                .filter(report -> ids.contains(report.getFpvReportId()))
                .toList();

        if (reportsToDelete.size() != ids.size()) {
            throw new AccessDeniedException("Ви намагаєтесь видалити звіти, які вам не належать, або яких не існує");
        }

        log.info("Pilot {} is deleting reports: {}", currentUsername, ids);
        fpvReportRepository.deleteAll(reportsToDelete);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        return auth.getName();
    }

    private FpvReport mapToEntity(FpvReportCreateRequest request) {
        // Якщо прийшов конкретний юзернейм (наприклад, з Telegram), беремо його.
        // Якщо ні — беремо chatId як ідентифікатор.
        String identifier = request.getCreatedByUsername();

        if (identifier == null || identifier.isBlank()) {
            identifier = "unknown_pilot";
        }

        final String finalIdentifier = identifier;

        // Шукаємо або створюємо пілота за цим ідентифікатором (chatId)
        FpvPilot fpvPilot = fpvPilotRepository.findByUsername(finalIdentifier)
                .orElseGet(() -> {
                    return fpvPilotRepository.save(FpvPilot.builder()
                            .username(finalIdentifier)
                            .firstname("Telegram User")
                            .lastname(finalIdentifier)
                            .password("pass_" + finalIdentifier)
                            .clientId("bot_" + finalIdentifier)
                            .build());
                });

        return FpvReport.builder()
                .fpvPilot(fpvPilot)
                .fpvDrone(fpvDroneService.mapToFpvDroneEntity(request.getFpvDrone()))
                .dateTimeFlight(updateDateTimeFlight(request.getDateTimeFlight()))
                .isLostFPVDueToREB(request.isLostFPVDueToREB())
                .isOnTargetFPV(request.isOnTargetFPV())
                .coordinatesMGRS(request.getCoordinatesMGRS())
                .additionalInfo(request.getAdditionalInfo())
                .createdByUsername(finalIdentifier) // ТУТ тепер буде chatId
                .build();
    }

    private FpvReportResponse mapToResponse(FpvReport fpvReport) {
        return FpvReportResponse.builder()
                .fpvReportId(fpvReport.getFpvReportId())
                .fpvDrone(fpvDroneService.mapToFPVDroneResponse(fpvReport.getFpvDrone()))
                .pilotUsername(fpvReport.getCreatedByUsername())
                .dateTimeFlight(updateDateTimeFlight(fpvReport.getDateTimeFlight()))
                .isLostFPVDueToREB(fpvReport.isLostFPVDueToREB())
                .isOnTargetFPV(fpvReport.isOnTargetFPV())
                .coordinatesMGRS(fpvReport.getCoordinatesMGRS())
                .additionalInfo(fpvReport.getAdditionalInfo())
                .build();
    }

    private LocalDateTime updateDateTimeFlight(LocalDateTime dateTimeFlight) {
        if  (dateTimeFlight == null) {
            dateTimeFlight = LocalDateTime.now();
        }
        return dateTimeFlight;
    }

    public Map<String, Object> getStatistics() {
        long total = fpvReportRepository.count();
        long hits = fpvReportRepository.countByIsOnTargetFPVTrue();
        long rebLosses = fpvReportRepository.countByIsLostFPVDueToREBTrue();

        // Рахуємо обриви за текстом, який ми самі ж додаємо в боті
        long fiberCuts = fpvReportRepository.countByAdditionalInfoContaining("Обрив оптоволокна");

        double accuracy = total > 0 ? (hits * 100.0 / total) : 0.0;

        return Map.of(
                "total", total,
                "hits", hits,
                "rebLosses", rebLosses,
                "fiberCuts", fiberCuts, // Нове поле для бота
                "accuracy", Math.round(accuracy * 10.0) / 10.0
        );
    }
}