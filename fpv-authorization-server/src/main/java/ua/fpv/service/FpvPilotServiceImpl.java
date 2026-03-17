package ua.fpv.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.fpv.entity.FpvPilot;
import ua.fpv.entity.request.FpvPilotAdminRequest;
import ua.fpv.entity.request.FpvPilotRequest;
import ua.fpv.entity.response.FpvPilotResponse;
import ua.fpv.entity.response.FpvPilotSignUpResponse;
import ua.fpv.entity.response.Role;
import ua.fpv.repository.FpvPilotRepository;
import ua.fpv.util.ClientNotFoundException;
import ua.fpv.util.FpvPilotNotFoundException;
import ua.fpv.util.UsernameIsAlreadyExistsException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FpvPilotServiceImpl implements FpvPilotService {

    private final FpvPilotRepository fpvPilotRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public FpvPilotSignUpResponse save(FpvPilotAdminRequest request) {
        if (fpvPilotRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameIsAlreadyExistsException("Username - " + request.getUsername() + " is already exists!");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String clientId = authentication.getName();
        log.info("clientId: {}", clientId);
        FpvPilot fpvPilot = mapToSignUpEntity(request, clientId);
        FpvPilot savedFpvPilot = fpvPilotRepository.save(fpvPilot);
        return mapToCreateFPVPilotResponse(savedFpvPilot);
    }

    public boolean existsById(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Optional<FpvPilot> targetPilotOpt = fpvPilotRepository.findById(id);

        if (targetPilotOpt.isEmpty()) {
            return false;
        }

        FpvPilot targetPilot = targetPilotOpt.get();

        FpvPilot currentUser = fpvPilotRepository.findByUsername(currentUsername)
                .orElse(null);

        if (currentUser == null) return false;

        boolean isOwner = currentUser.getFpvPilotId().equals(targetPilot.getFpvPilotId());
        boolean isCreator = targetPilot.getCreatedBy() != null &&
                targetPilot.getCreatedBy().getFpvPilotId().equals(currentUser.getFpvPilotId());

        return isOwner || isCreator;
    }

    @Transactional(readOnly = true)
    public FpvPilotSignUpResponse findById(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        FpvPilot currentUser = fpvPilotRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ClientNotFoundException("Поточний користувач не знайдений: " + currentUsername));

        FpvPilot targetPilot = fpvPilotRepository.findById(id)
                .orElseThrow(() -> new FpvPilotNotFoundException("Пілота з ID " + id + " не знайдено"));

        boolean isOwner = currentUser.getFpvPilotId().equals(targetPilot.getFpvPilotId());
        boolean isCreator = targetPilot.getCreatedBy() != null &&
                targetPilot.getCreatedBy().getFpvPilotId().equals(currentUser.getFpvPilotId());

        if (!isOwner && !isCreator) {
            throw new IllegalStateException("Доступ заборонено: ви можете переглядати тільки свій профіль або створених вами пілотів!");
        }

        return mapToCreateFPVPilotResponse(targetPilot);
    }

    @Transactional(readOnly = true)
    public List<FpvPilotSignUpResponse> findAll() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        List<FpvPilot> fpvPilots = fpvPilotRepository.findAllByUsernameOrCreatedByUsername(currentUsername, currentUsername);

        return fpvPilots.stream()
                .map(this::mapToCreateFPVPilotResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FpvPilotSignUpResponse update(Long id, FpvPilotAdminRequest fpvPilotRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        FpvPilot currentUser = fpvPilotRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ClientNotFoundException("Поточний користувач не знайдений"));

        FpvPilot existingFpvPilot = fpvPilotRepository.findById(id)
                .orElseThrow(() -> new FpvPilotNotFoundException("Пілот не знайдений"));

        boolean isOwner = existingFpvPilot.getFpvPilotId().equals(currentUser.getFpvPilotId());
        boolean isCreator = existingFpvPilot.getCreatedBy() != null &&
                existingFpvPilot.getCreatedBy().getFpvPilotId().equals(currentUser.getFpvPilotId());

        if (!isOwner && !isCreator) {
            throw new IllegalStateException("Доступ заборонено");
        }

        if (!existingFpvPilot.getUsername().equals(fpvPilotRequest.getUsername())) {
            if (fpvPilotRepository.findByUsername(fpvPilotRequest.getUsername()).isPresent()) {
                throw new UsernameIsAlreadyExistsException("Username " + fpvPilotRequest.getUsername() + " вже зайнятий!");
            }
        }

        existingFpvPilot.setUsername(fpvPilotRequest.getUsername());
        if (fpvPilotRequest.getPassword() != null && !fpvPilotRequest.getPassword().isEmpty()) {
            existingFpvPilot.setPassword(passwordEncoder.encode(fpvPilotRequest.getPassword()));
        }
        existingFpvPilot.setFirstname(fpvPilotRequest.getFirstname());
        existingFpvPilot.setLastname(fpvPilotRequest.getLastname());

        existingFpvPilot.getAuthorities().clear();
        if (fpvPilotRequest.getAuthorities() != null) {
            existingFpvPilot.getAuthorities().addAll(fpvPilotRequest.getAuthorities());
        }

        FpvPilot updatedFpvPilot = fpvPilotRepository.saveAndFlush(existingFpvPilot);

        return mapToCreateFPVPilotResponse(updatedFpvPilot);
    }

    @Transactional
    public void deleteById(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        FpvPilot currentUser = fpvPilotRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ClientNotFoundException("Поточний користувач не знайдений: " + currentUsername));

        FpvPilot targetPilot = fpvPilotRepository.findById(id)
                .orElseThrow(() -> new FpvPilotNotFoundException("Пілота з ID " + id + " не знайдено"));

        if (currentUser.getFpvPilotId().equals(targetPilot.getFpvPilotId())) {
            throw new IllegalStateException("You can't delete yourself!");
        }

        if (targetPilot.getCreatedBy() == null ||
                !targetPilot.getCreatedBy().getFpvPilotId().equals(currentUser.getFpvPilotId())) {
            throw new IllegalStateException("Доступ заборонено: ви можете видалити тільки тих пілотів, яких створили самі!");
        }

        log.info("Адмін {} видаляє пілота {}", currentUser.getUsername(), targetPilot.getUsername());

        fpvPilotRepository.delete(targetPilot);
    }

    public FpvPilotSignUpResponse mapToCreateFPVPilotResponse(FpvPilot pilot) {
        String creatorName = (pilot.getCreatedBy() != null) ? pilot.getCreatedBy().getUsername() : "system";
        String updaterName = (pilot.getUpdatedBy() != null) ? pilot.getUpdatedBy().getUsername() : creatorName;

        return FpvPilotSignUpResponse.builder()
                .id(pilot.getFpvPilotId())
                .firstname(pilot.getFirstname())
                .lastname(pilot.getLastname())
                .username(pilot.getUsername())
                .authorities(pilot.getAuthorities())
                .createdBy(creatorName)
                .updatedBy(updaterName)
                .createdAt(pilot.getCreatedAt())
                .updatedAt(pilot.getUpdatedAt())
                .build();
    }

    public FpvPilot mapToCreateFPVPilotEntity(FpvPilotAdminRequest fpvPilotAdminRequest) {
        return FpvPilot.builder()
                .username(fpvPilotAdminRequest.getUsername())
                .password(passwordEncoder.encode(fpvPilotAdminRequest.getPassword()))
                .firstname(fpvPilotAdminRequest.getFirstname())
                .lastname(fpvPilotAdminRequest.getLastname())
                .authorities(fpvPilotAdminRequest.getAuthorities())
                .build();
    }

    public FpvPilot mapToFPVPilotEntity(FpvPilotRequest request) {
        return FpvPilot.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .build();
    }

    public FpvPilotResponse mapToFPVPilotResponse(FpvPilot fpvPilot) {
        return FpvPilotResponse.builder()
                .firstname(fpvPilot.getFirstname())
                .lastname(fpvPilot.getLastname())
                .build();
    }

    private FpvPilot mapToSignUpEntity(FpvPilotAdminRequest request, String clientId) {
        Set<String> authorities = request.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            authorities = Role.USER.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
        }

        return FpvPilot.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .clientId(clientId)
                .authorities(authorities)
                .build();
    }

}