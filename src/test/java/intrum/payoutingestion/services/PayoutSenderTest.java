package intrum.payoutingestion.services;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import intrum.payoutingestion.exception.ServiceErrorException;
import intrum.payoutingestion.model.PayoutRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class PayoutSenderTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    WebClient webClient;

    @InjectMocks
    PayoutSender sender;

    @Test
    void send_validCase() {
        var record = new PayoutRecord(
                randomNumeric(9),
                LocalDate.now().toString(),
                new BigDecimal("10.25"));

        when(webClient.post()
                .uri(anyString())
                .contentType(any(MediaType.class))
                .bodyValue(record)
                .retrieve()
                .toBodilessEntity())
                .thenReturn(Mono.empty());

        sender.send(record);

        verify(webClient.post()
                .uri("/payout")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(record)
                .retrieve())
                .toBodilessEntity();
    }


    @Test
    void send_retriesThreeTimesThenThrowsError() {
        var record = new PayoutRecord("id", "2025-08-07", new BigDecimal("100.55"));
        var counter = new AtomicInteger();
        Mono<ResponseEntity<Void>> errorMono = Mono.defer(() -> {
            counter.incrementAndGet();
            return Mono.error(new RuntimeException("Error"));
        });

        when(webClient.post()
                .uri(anyString())
                .contentType(any())
                .bodyValue(record)
                .retrieve()
                .toBodilessEntity())
                .thenReturn(errorMono);

        assertThatThrownBy(() -> sender.send(record))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessageContaining("Failed to send payout id after 3 attempts");

        assertThat(counter.get()).isEqualTo(3);

        verify(webClient.post()
                .uri("/payout")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(record)
                .retrieve()).toBodilessEntity();
    }
}