package intrum.payoutingestion.services;

import intrum.payoutingestion.model.PayoutRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PayoutRowMapper {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    public Optional<PayoutRecord> mapRow(String id, String dateRow, String amountRow) {
        if (Stream.of(id, dateRow, amountRow)
                .anyMatch(s -> s == null || s.isBlank())) {
            return Optional.empty();
        }

        try {
            LocalDate.parse(dateRow.trim(), ISO);
            var amount = new BigDecimal(amountRow.trim().replaceAll(",", "."));
            return Optional.of(new PayoutRecord(id.trim(), dateRow.trim(), amount));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
