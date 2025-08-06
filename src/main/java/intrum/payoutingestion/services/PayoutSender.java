package intrum.payoutingestion.services;

import intrum.payoutingestion.exception.ServiceErrorException;
import intrum.payoutingestion.model.PayoutRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayoutSender {

    private static final String URL = "http://intrum.mocklab.io/payout";

    private final WebClient webClient;

    public void send(PayoutRecord record) {
        try {
            webClient.post()
                    .uri(URL)
                    .bodyValue(record)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnSuccess(r -> log.info("Payout sent successfully {}", record))
                    .onErrorResume(e -> {
                        log.error("Failed to send payout {}", record.companyIdentityNumber(), e);
                        return Mono.empty();
                    })
                    .block();

        } catch (Exception ex) {
            log.error("Failed to send payout {}", record.companyIdentityNumber(), ex);
            throw new ServiceErrorException("Failed to send payout " + record.companyIdentityNumber(), ex);
        }
    }
}
