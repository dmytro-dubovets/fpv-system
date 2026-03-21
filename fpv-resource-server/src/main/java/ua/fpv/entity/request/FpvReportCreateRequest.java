package ua.fpv.entity.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.Column;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ua.fpv.entity.model.FlightResult;
import ua.fpv.entity.validation.localdatetime.LocalDateTimeCustomDeserializer;
import ua.fpv.entity.validation.localdatetime.LocalDateTimeCustomSerializer;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FpvReportCreateRequest {

    private Long fpvPilotId;

    private String createdByUsername;

    @Valid
    @NotNull(message = "FPVDrone object can't be null!")
    private FpvDroneRequest fpvDrone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonSerialize(using = LocalDateTimeCustomSerializer.class)
    @JsonDeserialize(using = LocalDateTimeCustomDeserializer.class)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime dateTimeFlight;

    private boolean isLostFPVDueToREB;

    private FlightResult flightResult;

    @NotBlank(message = "Сoordinates MRGS are required!")
    private String coordinatesMGRS;

    @NotBlank(message = "Additional info is required!")
    private String additionalInfo;
}

