package intrum.payoutingestion.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PayoutIngestionTest {

    private static final WireMockServer WIREMOCK = new WireMockServer(0);

    @Value("classpath:integration-tests/payout/csv/wakanda.csv")
    Resource wakandaCsvFile;

    @TempDir
    static Path wakandaDir;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add("payout.api.url", () -> "http://localhost:" + WIREMOCK.port() + "/payout");
        propertyRegistry.add("payout.api.maxAttempts", () -> 1);
        propertyRegistry.add("payout.wakanda.folder", () -> wakandaDir.toString());
    }

    @BeforeAll
    static void startStub() {
        WIREMOCK.start();
    }

    @AfterAll
    static void stopStub() {
        WIREMOCK.stop();
    }

    @LocalServerPort
    int port;

    @Autowired
    WebClient.Builder webClientBuilder;

    @Test
    void endToEndProcess_valid() throws IOException {
        var yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);
        var targetFile = "WK_payouts_%s_120000.csv".formatted(yesterday);
        Files.copy(wakandaCsvFile.getInputStream(), wakandaDir.resolve(targetFile));

        WIREMOCK.stubFor(post("/payout")
                .willReturn(aResponse().withStatus(200)));

        var http = webClientBuilder.build();
        http.post()
                .uri("http://localhost:%d/api/payout/process".formatted(port))
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .toBodilessEntity()
                .block();

        WIREMOCK.verify(3, postRequestedFor(urlEqualTo("/payout")));
        assertThat(WIREMOCK.getAllServeEvents()).hasSize(3);
    }
}
