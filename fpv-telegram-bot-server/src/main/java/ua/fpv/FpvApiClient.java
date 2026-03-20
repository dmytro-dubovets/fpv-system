package ua.fpv;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ua.fpv.entity.FpvReportCreateRequest;

import java.util.Map;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class FpvApiClient {

    private final WebClient webClient;

    public Mono<Void> sendReport(FpvReportCreateRequest request) {
        return webClient.post()
                .uri("/api/v1/fpvreports")
                .attributes(clientRegistrationId("fpv-bot"))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Map<String, Object>> getStats() {
        return webClient.get()
                .uri("/api/v1/fpvreports/stats")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Mono<byte[]> downloadExcelReports() {
        return webClient.get()
                .uri("/api/v1/fpvreports/export/excel")
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(byte[].class);
    }

    // Допоміжний метод для зручності
    private static Consumer<Map<String, Object>> clientRegistrationId(String registrationId) {
        return ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId(registrationId);
    }
}
