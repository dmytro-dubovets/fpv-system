package ua.fpv.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.fpv.entity.model.FpvDrone;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FpvDroneResponse {

    private String fpvSerialNumber;

    private String fpvCraftName;

    private FpvDrone.FpvModel fpvModel;
}
