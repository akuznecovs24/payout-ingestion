package intrum.payoutingestion.services;

import intrum.payoutingestion.exception.ServiceErrorException;
import intrum.payoutingestion.model.PayoutRecord;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

@Service
@Slf4j
public class PayoutSender {

    private final String url;
    private final int maxAttempts;
    private final WebClient webClient;

    @Autowired
    public PayoutSender(@Value("${payout.api.uri}") String url, @Value("${payout.api.max-attempts}") int maxAttempts, WebClient webClient) {
        this.url = url;
        this.maxAttempts = maxAttempts;
        this.webClient = webClient;
    }

    PayoutSender (String url, int maxAttempts) {
        this.url = url;
        this.maxAttempts = maxAttempts;
        this.webClient = WebClient.create();
    }

    public void send(PayoutRecord record) {
        webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(record)
                .retrieve()
                .toBodilessEntity()
                .retryWhen(Retry.backoff(maxAttempts - 1, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(5))
                        .jitter(0.3)
                        .doBeforeRetry(rs ->
                                log.warn("Retry {}/{} for {}", rs.totalRetries() + 1, maxAttempts, record.companyIdentityNumber())))
                .onErrorMap(ex -> new ServiceErrorException(
                        "Failed to send payout %s after %d attempts".formatted(record.companyIdentityNumber(), maxAttempts), ex))
                .doOnSuccess(r -> log.info("Payout sent successfully {}", record))
                .block();
    }
}
