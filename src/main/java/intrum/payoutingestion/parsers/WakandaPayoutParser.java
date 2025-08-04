package intrum.payoutingestion.parsers;

import intrum.payoutingestion.exception.ServiceErrorException;
import intrum.payoutingestion.model.PayoutRecord;
import intrum.payoutingestion.model.SourcePayload;
import intrum.payoutingestion.services.PayoutRowMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;

@Slf4j
@RequiredArgsConstructor
public class WakandaPayoutParser implements CountryPayoutParser {

    private static final char DELIMITER = ';';
    private static final char QUOTE = '"';
    private static final String ID = "Company tax number";
    private static final String DATE = "Payment Date";
    private static final String AMOUNT = "Amount";

    private final PayoutRowMapper payoutRowMapper;

    @Override
    public boolean canParse(File file) {
        return file.getName().startsWith("WK_payouts");
    }

    @Override
    public List<PayoutRecord> parse(SourcePayload payload) {
        var payouts = new ArrayList<PayoutRecord>();
        var csvFormat = CSVFormat.DEFAULT
                .builder()
                .setDelimiter(DELIMITER)
                .setQuote(QUOTE)
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        try (
                var csvMessage = csvFormat.parse(new InputStreamReader(payload.inputStream(), StandardCharsets.UTF_8));

        ) {
            csvMessage.forEach(record ->
                    payoutRowMapper.mapRow(record.get(ID), record.get(DATE), record.get(AMOUNT))
                            .ifPresent(payouts::add));
        } catch (IOException e) {
            log.error("Error while reading file {}", payload.name(), e);
            throw new ServiceErrorException("Error while reading file " + e);
        }

        return payouts;
    }
}
