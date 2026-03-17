package ua.fpv.entity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data // Створить геттери, сеттери, equals, hashCode та toString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FpvPilot {

    private Long fpvPilotId;

    private String firstname;

    private String lastname;

    private String username;
}
