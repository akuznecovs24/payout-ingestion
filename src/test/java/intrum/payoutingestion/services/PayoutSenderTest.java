package intrum.payoutingestion.services;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import intrum.payoutingestion.exception.ServiceErrorException;
import intrum.payoutingestion.model.PayoutRecord;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class PayoutSenderTest {

    @Mock
    WebClient webClient;
    @Mock
    WebClient.RequestBodyUriSpec request;
    @Mock
    WebClient.RequestHeadersSpec<?> headers;
    @Mock
    WebClient.ResponseSpec response;

    private final PayoutRecord record =
            new PayoutRecord(randomNumeric(6), "2025-01-01", new BigDecimal("100.00"));

    private PayoutSender sender() {
        return new PayoutSender("/payout", 3, webClient);
    }

    @BeforeEach
    void setup() {

        when(webClient.post()).thenReturn(request);
        when(request.uri("/payout")).thenReturn(request);
        when(request.contentType(MediaType.APPLICATION_JSON)).thenReturn(request);
        Mockito.<WebClient.RequestHeadersSpec<?>>when(request.bodyValue(any()))
                .thenReturn(headers);
        when(headers.retrieve()).thenReturn(response);
    }

    @Test
    void send_valid() {
        when(response.toBodilessEntity()).thenReturn(Mono.empty());

        sender().send(record);

        verify(request).uri("/payout");
        verify(request).contentType(MediaType.APPLICATION_JSON);
        verify(request).bodyValue(record);
        verify(headers).retrieve();
        verify(response).toBodilessEntity();
    }

    @Test
    void send_retryThreeTimes_thenThrow() {
        var attempts = new AtomicInteger();

        when(response.toBodilessEntity())
                .thenReturn(Mono.defer(() -> {
                    attempts.incrementAndGet();
                    return Mono.error(new RuntimeException("ERROR"));
                }));

        assertThatThrownBy(() -> sender().send(record))
                .isInstanceOf(ServiceErrorException.class);

        assertThat(attempts.get()).isEqualTo(3);

    }
}