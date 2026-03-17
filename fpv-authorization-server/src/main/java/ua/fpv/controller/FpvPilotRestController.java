package ua.fpv.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.fpv.entity.request.FpvPilotAdminRequest;
import ua.fpv.entity.response.FpvPilotSignUpResponse;
import ua.fpv.service.FpvPilotServiceImpl;

import ua.fpv.util.FpvPilotNotFoundException;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fpvpilots")
public class FpvPilotRestController {

    private final FpvPilotServiceImpl fpvPilotServiceImpl;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_user:read')")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        FpvPilotSignUpResponse fpvPilotResponse = fpvPilotServiceImpl.findById(id);
        return ResponseEntity.ok(fpvPilotResponse);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_user:read')")
    public ResponseEntity<?> findAll() {
        List<FpvPilotSignUpResponse> fpvPilotsResponse = fpvPilotServiceImpl.findAll();
        return ResponseEntity.of(Optional.of(fpvPilotsResponse));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_user:write')")
    public ResponseEntity<?> save(@Valid @RequestBody FpvPilotAdminRequest fpvPilotRequest) {
        FpvPilotSignUpResponse fpvPilotResponse = fpvPilotServiceImpl.save(fpvPilotRequest);
        log.info("Fpv pilot response: {}", fpvPilotResponse.toString());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(fpvPilotResponse.getId())
                .toUri();

        return ResponseEntity.created(location).body(fpvPilotResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_user:write')")
    public ResponseEntity<?> update(@Valid @PathVariable Long id, @RequestBody FpvPilotAdminRequest fpvPilotRequest) {
        if (!fpvPilotServiceImpl.existsById(id)) {
            throw new FpvPilotNotFoundException("FPV pilot with id - " + id + " is not found!");
        }
        FpvPilotSignUpResponse fpvPilotResponse = fpvPilotServiceImpl.update(id, fpvPilotRequest);
        return ResponseEntity.ok(fpvPilotResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_user:write')")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
            fpvPilotServiceImpl.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).build();
    }
}
