package ua.fpv.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.TimeZone;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FpvReportRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    @DisplayName("GET /api/v1/fpvreports - Success with correct scope")
    void findAll_ShouldReturnOk_WhenAuthenticatedWithCorrectScope() throws Exception {
        mockMvc.perform(get("/api/v1/fpvreports")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_fpvreport:read"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/fpvreports - Forbidden with wrong scope")
    void save_ShouldReturnForbidden_WhenScopeIsMissing() throws Exception {
        String jsonBody = """
                {
                    "dateTimeFlight": "2026-03-12 17:45",
                    "isLostFPVDueToREB": false,
                    "isOnTargetFPV": true,
                    "coordinatesMGRS": "36UXV4567812345",
                    "additionalInfo": "Bakhmut",
                    "fpvDrone": {
                        "fpvSerialNumber": "SN-TEST-123",
                        "fpvCraftName": "Test Drone",
                        "fpvModel": "KAMIKAZE"
                    }
                }
                """;

        mockMvc.perform(post("/api/v1/fpvreports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_user:read"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/fpvreports - Unauthorized without token")
    void findAll_ShouldReturnUnauthorized_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/fpvreports"))
                .andExpect(status().isUnauthorized());
    }
}
