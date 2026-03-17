package ua.fpv.repository;

import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.fpv.entity.FpvDrone;
import ua.fpv.entity.FpvReport;

import java.util.List;

@Repository
public interface FpvReportRepository extends JpaRepository<FpvReport, Long> {

    @Override
    @NonNull
    List<FpvReport> findAll();

    List<FpvReport> findAllByFpvDrone_FpvModel(FpvDrone.FpvModel fpvModel);

    List<FpvReport> findAllByIsLostFPVDueToREB(Boolean isLostFPVDueToREB);

    List<FpvReport> findAllByIsOnTargetFPV(Boolean isOnTarget);

    List<FpvReport> findAllByFpvDrone_FpvSerialNumber(String serialNumber);
}
