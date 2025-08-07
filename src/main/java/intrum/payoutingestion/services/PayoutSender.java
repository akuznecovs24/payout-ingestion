package intrum.payoutingestion.services;

import intrum.payoutingestion.exception.ServiceErrorException;
import intrum.payoutingestion.model.PayoutRecord;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayoutSender {

    private static final String URL = "http://intrum.wiremockapi.cloud/payout";
    private static final int MAX_ATTEMPTS = 3;

    private final WebClient webClient;

    public void send(PayoutRecord record) {
        webClient.post()
                .uri(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(record)
                .retrieve()
                .toBodilessEntity()
                .retryWhen(Retry.backoff(MAX_ATTEMPTS - 1, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(5))
                        .jitter(0.3)
                        .doBeforeRetry(rs ->
                                log.warn("Retry {}/{} for {}", rs.totalRetries() + 1, MAX_ATTEMPTS, record.companyIdentityNumber())))
                .onErrorMap(ex -> new ServiceErrorException(
                        "Failed to send payout %s after %d attempts".formatted(record.companyIdentityNumber(), MAX_ATTEMPTS), ex))
                .doOnSuccess(r -> log.info("Payout sent successfully {}", record))
                .block();
    }
}
