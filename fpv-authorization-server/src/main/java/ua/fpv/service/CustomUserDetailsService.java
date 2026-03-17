package ua.fpv.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.fpv.config.CustomUserDetails;
import ua.fpv.entity.FpvPilot;
import ua.fpv.repository.FpvPilotRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    private final FpvPilotRepository fpvPilotRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        FpvPilot fpvPilot = fpvPilotRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<SimpleGrantedAuthority> authorities = fpvPilot.getAuthorities()
                .stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new CustomUserDetails(
                fpvPilot.getUsername(),
                fpvPilot.getPassword(),
                authorities);
    }
}
