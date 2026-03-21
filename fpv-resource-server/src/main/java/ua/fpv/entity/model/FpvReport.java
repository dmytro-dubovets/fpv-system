package ua.fpv.entity.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Builder
@EntityListeners(AuditingEntityListener.class)
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

    //@CreatedBy
    @Column(name = "created_by_username", updatable = false, nullable = false)
    private String createdByUsername;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_lost_fpv_due_to_reb")
    private boolean isLostFPVDueToREB;

    @Enumerated(EnumType.STRING)
    @Column(name = "flight_result")
    private FlightResult flightResult;

    @Column(name = "coordinates_mgrs")
    private String coordinatesMGRS;

    @Column(name = "additional_info")
    private String additionalInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fpv_pilot_id", nullable = false) // Вказуємо на існуючу колонку в БД
    private FpvPilot fpvPilot;

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