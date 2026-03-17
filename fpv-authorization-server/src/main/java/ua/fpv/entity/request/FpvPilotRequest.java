package ua.fpv.entity.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FpvPilotRequest {

    @NotBlank(message = "FPV pilot firstname is required!")
    private String firstname;

    @NotBlank(message = "FPV pilot lastname is required!")
    private String lastname;

}