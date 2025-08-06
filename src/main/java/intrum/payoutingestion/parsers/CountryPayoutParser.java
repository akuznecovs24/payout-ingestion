package intrum.payoutingestion.parsers;

import intrum.payoutingestion.model.PayoutRecord;
import intrum.payoutingestion.model.SourcePayload;
import java.util.List;

public interface CountryPayoutParser {

    String countryCode();

    List<PayoutRecord> parse(SourcePayload payload);

}
