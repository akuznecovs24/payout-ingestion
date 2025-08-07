package intrum.payoutingestion.services;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;

import intrum.payoutingestion.model.PayoutRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PayoutRowMapperTest {

    private final PayoutRowMapper mapper = new PayoutRowMapper();

    @Test
    void mapRow_valid() {
        var id = randomAlphanumeric(8);
        var date = LocalDate.now().toString();
        var amount = "1234,56";

        var result = mapper.mapRow(id, date, amount);

        assertThat(result)
                .isPresent()
                .contains(new PayoutRecord(id, date, new BigDecimal("1234.56")));
    }

    @Test
    void mapRow_blankField_returnsEmpty() {
        var result = mapper.mapRow("", "2025-08-07", "1,0");

        assertThat(result).isEmpty();
    }

    @Test
    void mapRow_invalidDate_returnsEmpty() {
        var id = randomAlphanumeric(6);
        var result = mapper.mapRow(id, "2025-13-40", "10");

        assertThat(result).isEmpty();
    }

    @Test
    void mapRow_invalidAmountReturnsEmpty() {
        var id = randomAlphanumeric(6);
        var date = LocalDate.now().toString();

        var result = mapper.mapRow(id, date, "abc");

        assertThat(result).isEmpty();
    }

    @Test
    void mapRow_trimsSpaces() {
        var id = "  " + randomAlphanumeric(5) + "  ";
        var date = " 2025-08-07 ";
        var amount = " 7,00 ";

        var record = mapper.mapRow(id, date, amount).orElseThrow();

        assertThat(record.companyIdentityNumber()).isEqualTo(id.trim());
        assertThat(record.paymentDate()).isEqualTo(date.trim());
        assertThat(record.paymentAmount()).isEqualByComparingTo("7.00");
    }
}