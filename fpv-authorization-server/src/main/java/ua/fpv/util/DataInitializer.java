package ua.fpv.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ua.fpv.entity.FpvDrone;
import ua.fpv.entity.FpvPilot;
import ua.fpv.entity.FpvReport;
import ua.fpv.repository.FpvPilotRepository;
import ua.fpv.repository.FpvReportRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final FpvPilotRepository fpvPilotRepository;

    private final FpvReportRepository fpvReportRepository;

    private final PasswordEncoder passwordEncoder;

    private final RegisteredClientRepository registeredClientRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initData() {
        if (registeredClientRepository.findByClientId("fpv-client") == null) {
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("fpv-client")
                .clientSecret(passwordEncoder.encode("fpv-secret"))
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri("https://unadjunctively-scissorlike-brynn.ngrok-free.dev/login/oauth2/code/fpv-bot")
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .scope(OidcScopes.OPENID)
                .scope("fpvreport:write")
                .scope("fpvreport:read")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .refreshTokenTimeToLive(Duration.ofDays(30))
                        .build())
                .build();

        registeredClientRepository.save(client);

        }
    }
}

