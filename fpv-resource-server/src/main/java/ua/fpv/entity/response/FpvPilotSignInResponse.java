package ua.fpv.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FpvPilotSignInResponse {

    private String token;

    private long expiresIn;

    private String refreshToken;

}