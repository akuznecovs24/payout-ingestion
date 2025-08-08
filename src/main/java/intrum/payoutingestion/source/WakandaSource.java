package intrum.payoutingestion.source;

import static java.util.Comparator.comparing;

import intrum.payoutingestion.exception.ServiceErrorException;
import intrum.payoutingestion.model.SourcePayload;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WakandaSource implements CountryPayoutSource {

    private static final Pattern WK_PATTERN = Pattern.compile("^WK_payouts_(\\d{8})_(\\d{6})\\.csv$");

    private final Path folder;

    @Autowired
    public WakandaSource(@Value("${payout.wakanda.folder}") String folder) {
        this.folder = Paths.get(folder);
    }

    WakandaSource(Path folder) {
        this.folder = folder;
    }

    @Override
    public String countryCode() {
        return "WK";
    }

    @Override
    public Optional<SourcePayload> fetch() {
        var yesterday = LocalDate.now().minusDays(1);

        try (var files = Files.list(folder)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> isForDate(path.getFileName().toString(), yesterday))
                    .max(comparing(path -> extractTime(path.getFileName().toString())))
                    .map(this::toPayload);
        } catch (IOException e) {
            throw new ServiceErrorException("Cannot list " + folder + " " + e.getMessage(), e);
        }
    }

    private boolean isForDate(String name, LocalDate date) {
        var matcher = WK_PATTERN.matcher(name);
        return matcher.matches() && matcher.group(1).equals(date.format(DateTimeFormatter.BASIC_ISO_DATE));
    }

    private int extractTime(String name) {
        var matcher = WK_PATTERN.matcher(name);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(2));
        }
        return 0;
    }

    private SourcePayload toPayload(Path path) {
        try {
            return new SourcePayload(
                    path.getFileName().toString(),
                    Files.newInputStream(path));
        } catch (IOException e) {
            log.error("Payload can't be created {}", e.getMessage(), e);
            throw new ServiceErrorException("Payload can't be created " + e.getMessage(), e);
        }
    }
}
