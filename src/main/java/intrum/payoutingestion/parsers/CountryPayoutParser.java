package intrum.payoutingestion.parsers;

import intrum.payoutingestion.model.PayoutRecord;
import intrum.payoutingestion.model.SourcePayload;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public interface CountryPayoutParser {

    boolean canParse(File file);

    List<PayoutRecord> parse(SourcePayload payload) throws FileNotFoundException;

}
