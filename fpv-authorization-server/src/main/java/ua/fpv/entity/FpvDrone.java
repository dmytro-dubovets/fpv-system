package ua.fpv.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Builder
@Table(name = "fpv_drone")
public class FpvDrone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fpv_drone_id")
    @JsonIgnore
    private Long fpvDroneId;

    @Column(name = "fpv_serial_number")
    private String fpvSerialNumber;

    @Column(name = "fpv_craft_name")
    private String fpvCraftName;

    @Enumerated(EnumType.STRING)
    @Column(name = "fpv_model")
    private FpvModel fpvModel;

    @OneToOne(mappedBy = "fpvDrone")
    @JsonIgnore
    private FpvReport fpvReport;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FpvDrone other)) return false;
        if (this.fpvDroneId == null || other.fpvDroneId == null) return false;
        return this.fpvDroneId.equals(other.fpvDroneId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fpvDroneId);
    }

    public enum FpvModel {

        KAMIKAZE, BOMBER, PPO;
    }

}
