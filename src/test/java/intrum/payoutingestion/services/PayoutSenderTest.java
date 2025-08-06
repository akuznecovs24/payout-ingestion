package intrum.payoutingestion.services;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import intrum.payoutingestion.model.PayoutRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class PayoutSenderTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    WebClient webClient;

    PayoutSender payoutSender;

    @BeforeEach
    void setUp() {
        payoutSender = new PayoutSender(webClient);
    }

    @Test
    void send_successful_call() {
        var record = new PayoutRecord("123456-7", LocalDate.now(), new BigDecimal("100.00"));

        when(webClient.post()
                .uri(anyString())
                .bodyValue(any())
                .retrieve()
                .toBodilessEntity())
                .thenReturn(Mono.just(ResponseEntity.ok().build()));

        assertThatCode(() -> payoutSender.send(record))
                .doesNotThrowAnyException();

        verify(webClient.post()).uri("http://intrum.mocklab.io/payout");
        verify(webClient.post().bodyValue(record));
    }

    @Test
    void send_remote_returns_error_but_is_swallowed() {
        var record = new PayoutRecord("123456-7", LocalDate.now(), new BigDecimal("100.00"));

        when(webClient.post()
                .uri(anyString())
                .bodyValue(any())
                .retrieve()
                .toBodilessEntity())
                .thenReturn(Mono.error(new IllegalStateException("Error")));

        assertThatCode(() -> payoutSender.send(record))
                .doesNotThrowAnyException();
    }
}