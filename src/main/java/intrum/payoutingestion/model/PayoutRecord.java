package intrum.payoutingestion.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayoutRecord (String companyIdentityNumber, LocalDate payoutDate, BigDecimal paymentAmount) {

}
