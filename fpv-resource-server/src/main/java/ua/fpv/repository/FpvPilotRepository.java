package ua.fpv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.fpv.entity.model.FpvPilot;

public interface FpvPilotRepository extends JpaRepository<FpvPilot, Long> {

}
