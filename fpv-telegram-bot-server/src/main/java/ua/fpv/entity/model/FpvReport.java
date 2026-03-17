package ua.fpv.entity.model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FpvReport implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long fpvReportId;

    private FpvDrone fpvDrone;

    private LocalDateTime dateTimeFlight;

    // Це поле можна залишити, щоб сервер знав, який пілот надіслав звіт (наприклад, Telegram ID або Username)
    private String createdByUsername;

    private boolean isLostFPVDueToREB;

    private boolean isOnTargetFPV;

    private String coordinatesMGRS;

    private String additionalInfo;
}