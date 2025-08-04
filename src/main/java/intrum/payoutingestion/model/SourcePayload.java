package intrum.payoutingestion.model;

import java.io.InputStream;

public record SourcePayload(String name, InputStream inputStream) {

}
