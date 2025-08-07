package intrum.payoutingestion.services;

import intrum.payoutingestion.exception.ServiceErrorException;
import intrum.payoutingestion.model.SourcePayload;
import intrum.payoutingestion.parsers.CountryPayoutParser;
import intrum.payoutingestion.source.CountryPayoutSource;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayoutProcessor {

    private final List<CountryPayoutSource> sources;
    private final Map<String, CountryPayoutParser> parserMap;
    private final PayoutSender sender;

    @Scheduled(cron = "0 30 0 * * *")
    public void process() {
        for (var source : sources) {
            try {
                source.fetch().ifPresent(payload -> handlePayload(source, payload));
            } catch (Exception ex) {
                log.error("Fetch failed for {}, error: {}", source.countryCode(), ex.getMessage(), ex);
                throw new ServiceErrorException("Fetch failed for %s".formatted(source.countryCode()), ex);
            }
        }
    }

    private void handlePayload(CountryPayoutSource source, SourcePayload payload) {
        var parser = parserMap.get(source.countryCode());

        if (parser == null) {
            log.warn("No parser for {}", source.countryCode());
            return;
        }

        var records = parser.parse(payload);

        records.forEach(record -> {
            try {
                sender.send(record);
            } catch (Exception ex) {
                log.error("Send failed {}", record.companyIdentityNumber(), ex);
            }
        });
    }
}
