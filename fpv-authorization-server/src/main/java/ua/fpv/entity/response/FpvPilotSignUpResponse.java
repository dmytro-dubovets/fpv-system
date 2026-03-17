package ua.fpv.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FpvPilotSignUpResponse {

    private Long id;

    private String firstname;

    private String lastname;

    private String username;

    private Set<String> authorities;

    private String createdBy;

    private String updatedBy;

    private Date createdAt;

    private Date updatedAt;

}
