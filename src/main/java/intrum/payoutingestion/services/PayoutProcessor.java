package intrum.payoutingestion.services;

import intrum.payoutingestion.exception.ServiceErrorException;
import intrum.payoutingestion.model.SourcePayload;
import intrum.payoutingestion.parsers.CountryPayoutParser;
import intrum.payoutingestion.source.CountryPayoutSource;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayoutProcessor {

    private final List<CountryPayoutSource> sources;
    private final Map<String, CountryPayoutParser> parserMap;
    private final PayoutSender sender;

    //    @Scheduled(cron = "0 0 0 * * *")
    @PostConstruct
    public void process() {
        sources.forEach(this::handleSource);
    }

    private void handleSource(CountryPayoutSource source) {
        var fetchedPayload = source.fetch();

        //TODO handle errors here - try catch
        fetchedPayload.ifPresent(payload -> handlePayload(source, payload));
    }

    private void handlePayload(CountryPayoutSource source, SourcePayload payload) {
        var parser = parserMap.get(source.countryCode());

        if (parser == null) {
            log.warn("No parser registered for country {}", source.countryCode());
            return;
        }

        try {
            parser.parse(payload).forEach(sender::send);
        } catch (Exception ex) {
            log.error("Parse failed for {}, error: {}", payload.name(), ex.getMessage(), ex);
            throw new ServiceErrorException("Parse failed for %s".formatted(payload.name()), ex);
        }
    }
}
