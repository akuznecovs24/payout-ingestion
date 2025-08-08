package intrum.payoutingestion.parsers;

import intrum.payoutingestion.exception.ServiceErrorException;
import intrum.payoutingestion.model.PayoutRecord;
import intrum.payoutingestion.model.SourcePayload;
import intrum.payoutingestion.services.PayoutRowMapper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WakandaPayoutParser implements CountryPayoutParser {

    private static final char DELIMITER = ';';
    private static final char QUOTE = '"';
    private static final String ID = "Company tax number";
    private static final String DATE = "Payment Date";
    private static final String AMOUNT = "Amount";

    private final PayoutRowMapper payoutRowMapper;

    @Override
    public String countryCode() {
        return "WK";
    }

    @Override
    public List<PayoutRecord> parse(SourcePayload payload) {
        var payouts = new ArrayList<PayoutRecord>();
        var csvFormat = CSVFormat.DEFAULT
                .builder()
                .setDelimiter(DELIMITER)
                .setQuote(QUOTE)
                .setHeader()
                .setAllowMissingColumnNames(true)
                .setSkipHeaderRecord(true)
                .build();

        try (
                var csvMessage = csvFormat.parse(new InputStreamReader(payload.inputStream(), StandardCharsets.ISO_8859_1))
        ) {
            csvMessage.forEach(record ->
                    payoutRowMapper.mapRow(record.get(ID), record.get(DATE), record.get(AMOUNT))
                            .ifPresentOrElse(payouts::add, () -> log.warn("Invalid record {}", record)));
        } catch (IOException e) {
            log.error("Error while reading file {}", payload.name(), e);
            throw new ServiceErrorException("Error while reading file " + e);
        }

        return payouts;
    }
}
