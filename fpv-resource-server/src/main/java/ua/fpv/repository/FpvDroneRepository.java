package ua.fpv.repository;

import org.springframework.data.repository.CrudRepository;
import ua.fpv.entity.model.FpvDrone;

public interface FpvDroneRepository extends CrudRepository<FpvDrone, Long> {
}
