package ua.fpv.entity.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.fpv.entity.model.FpvDrone;
import ua.fpv.entity.validation.fpvmodel.EnumNamePattern;
import ua.fpv.entity.validation.fpvmodel.FpvModelDeserializer;
import ua.fpv.entity.validation.fpvmodel.FpvModelSerializer;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FpvDroneRequest {

    @NotBlank(message = "FPV Serial Number is required!")
    private String fpvSerialNumber;

    @NotBlank(message = "FPV Craft Name is required!")
    private String fpvCraftName;

    @NotNull(message = "FPV Model is required!")
    @EnumNamePattern(regexp = "KAMIKAZE|BOMBER|PPO")
    private FpvDrone.FpvModel fpvModel;

    @JsonSerialize(using = FpvModelSerializer.class)
    @JsonDeserialize(using = FpvModelDeserializer.class)
    public enum FpvModel {

        KAMIKAZE, BOMBER, PPO;
    }

}
