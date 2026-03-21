package ua.fpv.entity.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.fpv.entity.model.FlightResult;
import ua.fpv.entity.validation.localdatetime.LocalDateTimeCustomDeserializer;
import ua.fpv.entity.validation.localdatetime.LocalDateTimeCustomSerializer;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FpvReportResponse {

    private Long fpvReportId;

    private FpvDroneResponse fpvDrone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonSerialize(using = LocalDateTimeCustomSerializer.class)
    @JsonDeserialize(using = LocalDateTimeCustomDeserializer.class)
    private LocalDateTime dateTimeFlight;

    private String pilotUsername;

    private boolean isLostFPVDueToREB;

    private FlightResult flightResult;

    private String coordinatesMGRS;

    private String additionalInfo;

}
