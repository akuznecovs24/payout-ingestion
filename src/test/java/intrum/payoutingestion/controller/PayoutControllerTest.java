package intrum.payoutingestion.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import intrum.payoutingestion.services.PayoutProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class PayoutControllerTest {

    @Mock
    private PayoutProcessor payoutProcessor;

    @InjectMocks
    private PayoutController payoutController;

    @Test
    void triggerProcessing() {
        doNothing().when(payoutProcessor).process();

        var response = payoutController.triggerProcessing();

        verify(payoutProcessor).process();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Payout processing triggered successfully");
    }
}