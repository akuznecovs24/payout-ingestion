package intrum.payoutingestion.model;

import java.math.BigDecimal;

public record PayoutRecord(String companyIdentityNumber, String paymentDate, BigDecimal paymentAmount) {

}
