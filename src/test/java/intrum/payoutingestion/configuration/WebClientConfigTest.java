package intrum.payoutingestion.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class WebClientConfigTest {

    @InjectMocks
    private WebClientConfig webClientConfig;

    @Test
    void webClient() {
        var result = webClientConfig.webClient();
        
        assertThat(result).isNotNull();
    }
}