package intrum.payoutingestion.parsers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import intrum.payoutingestion.exception.ServiceErrorException;
import intrum.payoutingestion.model.PayoutRecord;
import intrum.payoutingestion.model.SourcePayload;
import intrum.payoutingestion.services.PayoutRowMapper;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WakandaPayoutParserTest {

    @Mock
    private PayoutRowMapper payoutRowMapper;

    @InjectMocks
    private WakandaPayoutParser parser;

    @Test
    void countryCode() {
        var result = parser.countryCode();

        assertThat(result).isEqualTo("WK");
    }

    @Test
    void parse() {
        var companyId = RandomStringUtils.randomAlphanumeric(10);
        var date = LocalDate.now().toString();
        var amount = "100.00";
        var csvContent = "Company tax number;Payment Date;Amount\n" +
                companyId + ";" + date + ";" + amount;
        var inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.ISO_8859_1));
        var payload = new SourcePayload("test.csv", inputStream);
        var payoutRecord = new PayoutRecord(companyId, LocalDate.now(), new BigDecimal(amount));

        when(payoutRowMapper.mapRow(companyId, date, amount)).thenReturn(Optional.of(payoutRecord));

        var result = parser.parse(payload);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(payoutRecord);
    }

    @Test
    void parse_multipleRecords() {
        var companyId1 = RandomStringUtils.randomAlphanumeric(10);
        var companyId2 = RandomStringUtils.randomAlphanumeric(10);
        var date = LocalDate.now().toString();
        var amount1 = "100.00";
        var amount2 = "200.00";
        var csvContent = "Company tax number;Payment Date;Amount\n" +
                companyId1 + ";" + date + ";" + amount1 + "\n" +
                companyId2 + ";" + date + ";" + amount2;
        var inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.ISO_8859_1));
        var payload = new SourcePayload("test.csv", inputStream);
        var payoutRecord1 = new PayoutRecord(companyId1, LocalDate.now(), new BigDecimal(amount1));
        var payoutRecord2 = new PayoutRecord(companyId2, LocalDate.now(), new BigDecimal(amount2));

        when(payoutRowMapper.mapRow(companyId1, date, amount1)).thenReturn(Optional.of(payoutRecord1));
        when(payoutRowMapper.mapRow(companyId2, date, amount2)).thenReturn(Optional.of(payoutRecord2));

        var result = parser.parse(payload);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(payoutRecord1, payoutRecord2);
    }

    @Test
    void parse_skipInvalidRecord() {
        var companyId1 = RandomStringUtils.randomAlphanumeric(10);
        var companyId2 = RandomStringUtils.randomAlphanumeric(10);
        var date = LocalDate.now().toString();
        var amount1 = "100.00";
        var amount2 = "invalid";
        var csvContent = "Company tax number;Payment Date;Amount\n" +
                companyId1 + ";" + date + ";" + amount1 + "\n" +
                companyId2 + ";" + date + ";" + amount2;
        var inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.ISO_8859_1));
        var payload = new SourcePayload("test.csv", inputStream);
        var payoutRecord1 = new PayoutRecord(companyId1, LocalDate.now(), new BigDecimal(amount1));

        when(payoutRowMapper.mapRow(companyId1, date, amount1)).thenReturn(Optional.of(payoutRecord1));
        when(payoutRowMapper.mapRow(companyId2, date, amount2)).thenReturn(Optional.empty());

        var result = parser.parse(payload);

        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(payoutRecord1);
    }

    @Test
    void parse_ioException() {
        var payload = new SourcePayload("test.csv", new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Test exception");
            }
        });

        assertThatThrownBy(() -> parser.parse(payload))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessageContaining("Error while reading file");
    }
}