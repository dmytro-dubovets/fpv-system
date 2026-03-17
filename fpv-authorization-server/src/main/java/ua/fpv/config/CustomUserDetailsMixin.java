package ua.fpv.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CustomUserDetailsMixin {
    @JsonCreator
    CustomUserDetailsMixin(
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities) {}
}
