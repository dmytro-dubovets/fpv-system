package ua.fpv.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.fpv.entity.FpvPilot;

import java.util.List;
import java.util.Optional;

@Repository
public interface FpvPilotRepository extends JpaRepository<FpvPilot, Long> {

    @Override
    @NonNull
    Optional<FpvPilot> findById(Long id);

    Optional<FpvPilot> findByUsername(String username);

    Optional<FpvPilot> findByClientId(String clientId);

    @Override
    @NonNull
    List<FpvPilot> findAll();

    Optional<FpvPilot> findByClientIdAndUsername(String clientId, String username);

    List<FpvPilot> findAllByUsernameOrCreatedByUsername(String username, String createdByUsername);

}
