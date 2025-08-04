package intrum.payoutingestion.services;

import intrum.payoutingestion.model.PayoutRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class PayoutRowMapper {

    public Optional<PayoutRecord> mapRow(String companyId, String paymentDateRow, String amountRow) {
        if (Stream.of(companyId, paymentDateRow, amountRow)
                .anyMatch(s -> s == null || s.isBlank())) {
            return Optional.empty();
        }

        try {
            var paymentDate = LocalDate.parse(paymentDateRow.trim());
            var amount = new BigDecimal(amountRow.trim().replaceAll(",", "."));
            return Optional.of(new PayoutRecord(companyId.trim(), paymentDate, amount));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
