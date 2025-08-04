package intrum.payoutingestion.source;

import intrum.payoutingestion.model.SourcePayload;
import java.io.IOException;
import java.util.Optional;

public interface CountryPayoutSource {

    String countryCode();
    Optional<SourcePayload> fetch();
}
