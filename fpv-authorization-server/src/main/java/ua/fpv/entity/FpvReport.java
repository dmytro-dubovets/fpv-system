package ua.fpv.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Builder
@Table(name = "fpv_report")
public class FpvReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fpvReportId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "fpv_drone_id", referencedColumnName = "fpv_drone_id")
    private FpvDrone fpvDrone;

    @Column(name = "date_time_flight")
    private LocalDateTime dateTimeFlight;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "fpv_pilot_id", referencedColumnName = "fpv_pilot_id", nullable = false)
    private FpvPilot fpvPilot;

    @Column(name = "is_lost_fpv_due_to_reb")
    private boolean isLostFPVDueToREB;

    @Column(name = "is_on_target_fpv")
    private boolean isOnTargetFPV;

    @Column(name = "coordinates_mgrs")
    private String coordinatesMGRS;

    @Column(name = "additional_info")
    private String additionalInfo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FpvReport fpvReport)) return false;
        if (this.fpvReportId == null || fpvReport.fpvReportId == null) return false;
        return this.fpvReportId.equals(fpvReport.fpvReportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fpvReportId);
    }

}