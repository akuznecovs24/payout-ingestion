package intrum.payoutingestion.source;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import intrum.payoutingestion.exception.ServiceErrorException;
import intrum.payoutingestion.model.SourcePayload;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WakandaSourceTest {

    @TempDir
    Path temp;

    @Test
    void countryCode() {
        var source = new WakandaSource(temp);
        var result = source.countryCode();

        assertThat(result).isEqualTo("WK");
    }

    @Test
    void fetch_noFiles() {
        var source = new WakandaSource(temp);
        var result = source.fetch();

        assertThat(result).isEmpty();
    }

    @Test
    void fetch_noMatchingFiles() throws Exception {
        Files.writeString(temp.resolve("random.txt"), "x");

        var source = new WakandaSource(temp);
        var result = source.fetch();

        assertThat(result).isEmpty();
    }

    @Test
    void fetch_fileForDifferentDate() throws Exception {
        var tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);
        var name = "WK_payouts_%s_120000.csv".formatted(tomorrow);
        Files.writeString(temp.resolve(name), "csv");

        var source = new WakandaSource(temp);
        var result = source.fetch();

        assertThat(result).isEmpty();
    }

    @Test
    void fetch_singleFile() throws Exception {
        var yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);
        var name = "WK_payouts_%s_120000.csv".formatted(yesterday);
        var content = randomAlphabetic(20);
        Files.writeString(temp.resolve(name), content);

        var source = new WakandaSource(temp);
        var payload = source.fetch();

        assertThat(payload).isPresent();
        var bytes = payload.get().inputStream().readAllBytes();
        assertThat(new String(bytes)).isEqualTo(content);
        assertThat(payload.get().name()).isEqualTo(name);
    }

    @Test
    void fetch_multipleFilesReturnsLatest() throws Exception {
        var yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);

        Files.writeString(temp.resolve("WK_payouts_%s_100000.csv".formatted(yesterday)), "a");
        Files.writeString(temp.resolve("WK_payouts_%s_110000.csv".formatted(yesterday)), "b");
        var latest = "WK_payouts_%s_235900.csv".formatted(yesterday);
        Files.writeString(temp.resolve(latest), "c");

        var source = new WakandaSource(temp);
        var result = source.fetch();

        assertThat(result).isPresent()
                .map(SourcePayload::name)
                .hasValue(latest);
    }

    @Test
    void fetch_ioException() {
        var badPath = temp.resolve("no_such_dir");   // не существует
        var source = new WakandaSource(badPath);

        assertThatThrownBy(source::fetch)
                .isInstanceOf(ServiceErrorException.class)
                .hasCauseInstanceOf(NoSuchFileException.class);
    }
}