package ua.fpv.repository;

import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.fpv.entity.model.FlightResult;
import ua.fpv.entity.model.FpvReport;

import java.util.List;
import java.util.Optional;

public interface FpvReportRepository extends JpaRepository<FpvReport, Long> {

    @Override
    @NonNull
    List<FpvReport> findAll();

    List<FpvReport> findAllByCreatedByUsername(String username);

    Optional<FpvReport> findByFpvReportIdAndCreatedByUsername(Long id, String username);

    List<FpvReport> findAllByFpvDrone_FpvSerialNumber(String serialNumber);

    long count();

    long countByIsOnTargetFPVTrue();

    long countByIsLostFPVDueToREBTrue();

    long countByAdditionalInfoContaining(String text);

    long countByFlightResult(FlightResult flightResult);

}
