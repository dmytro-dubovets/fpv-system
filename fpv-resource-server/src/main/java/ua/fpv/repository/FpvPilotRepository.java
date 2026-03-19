package ua.fpv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.fpv.entity.model.FpvPilot;

import java.util.Optional;

public interface FpvPilotRepository extends JpaRepository<FpvPilot, Long> {

    Optional<FpvPilot> findByUsername(String currentUsername);
}
