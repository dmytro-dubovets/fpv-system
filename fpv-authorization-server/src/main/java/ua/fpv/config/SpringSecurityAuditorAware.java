package ua.fpv.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ua.fpv.entity.FpvPilot;
import ua.fpv.repository.FpvPilotRepository;

import java.util.Optional;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<FpvPilot> {

    private final FpvPilotRepository fpvPilotRepository;

    public SpringSecurityAuditorAware(FpvPilotRepository fpvPilotRepository) {
        this.fpvPilotRepository = fpvPilotRepository;
    }

    @Override
    @NonNull
    public Optional<FpvPilot> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String username = authentication.getName();

        return fpvPilotRepository.findByUsername(username);
    }
}