package ua.fpv.entity.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.fpv.entity.validation.authorities.ValidAuthority;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FpvPilotAdminRequest {

    @NotBlank(message = "Username must not be null or empty!")
    private String username;

    @NotBlank(message = "Password must not be null or empty!")
    private String password;

    @NotBlank(message = "Firstname must not be null or empty!")
    private String firstname;

    @NotBlank(message = "Lastname must not be null or empty!")
    private String lastname;

    @ValidAuthority
    @NotEmpty(message = "Authorities must not be empty!")
    //@JsonSerialize(using = AuthoritiesSerializer.class)
    //@JsonDeserialize(using = AuthoritiesDeserializer.class)
    private Set<String> authorities;

}

