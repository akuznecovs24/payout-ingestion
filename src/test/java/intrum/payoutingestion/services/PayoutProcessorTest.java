package intrum.payoutingestion.services;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import intrum.payoutingestion.exception.ServiceErrorException;
import intrum.payoutingestion.model.PayoutRecord;
import intrum.payoutingestion.model.SourcePayload;
import intrum.payoutingestion.parsers.CountryPayoutParser;
import intrum.payoutingestion.source.CountryPayoutSource;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class PayoutProcessorTest {

    @MockitoBean
    CountryPayoutSource source;

    @MockitoBean
    CountryPayoutParser parser;

    @MockitoBean
    Map<String, CountryPayoutParser> parserMap;

    @MockitoBean
    PayoutSender sender;

    @Autowired
    PayoutProcessor processor;

    private static final String WK_COUNTRY_CODE = "WK";

    @Test
    void process_valid() {
        var payload = new SourcePayload("file.csv", new ByteArrayInputStream("data".getBytes()));
        var record = new PayoutRecord(randomNumeric(5), LocalDate.now().toString(), new BigDecimal("1.00"));

        when(source.countryCode()).thenReturn(WK_COUNTRY_CODE);
        when(source.fetch()).thenReturn(Optional.of(payload));
        when(parserMap.get(WK_COUNTRY_CODE)).thenReturn(parser);
        when(parser.parse(payload)).thenReturn(List.of(record));

        processor.process();

        verify(parser).parse(payload);
        verify(sender).send(record);
    }

    @Test
    void process_noParserWarnsAndSkips() {
        var payload = new SourcePayload("file.csv", new ByteArrayInputStream("data".getBytes()));

        when(source.countryCode()).thenReturn(WK_COUNTRY_CODE);
        when(source.fetch()).thenReturn(Optional.of(payload));

        processor.process();

        verify(parser, never()).parse(payload);
        verify(sender, never()).send(any());
    }

    @Test
    void process_parserThrowsError() {
        var payload = new SourcePayload("file.csv",
                new ByteArrayInputStream("data".getBytes()));

        when(source.countryCode()).thenReturn(WK_COUNTRY_CODE);
        when(source.fetch()).thenReturn(Optional.of(payload));
        when(parserMap.get(WK_COUNTRY_CODE)).thenReturn(parser);
        when(parser.parse(payload)).thenThrow(new RuntimeException("Error"));

        assertThatThrownBy(() -> processor.process())
                .isInstanceOf(ServiceErrorException.class);

        verify(sender, never()).send(any());
    }
}