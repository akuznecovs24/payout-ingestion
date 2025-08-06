package intrum.payoutingestion.configuration;

import intrum.payoutingestion.parsers.CountryPayoutParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParserConfigTest {

    @Mock
    private CountryPayoutParser parser1;

    @Mock
    private CountryPayoutParser parser2;

    @InjectMocks
    private ParserConfig parserConfig;

    @Test
    void parserMap() {
        when(parser1.countryCode()).thenReturn("WK");
        when(parser2.countryCode()).thenReturn("US");

        var result = parserConfig.parserMap(List.of(parser1, parser2));

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsKeys("WK", "US");
        assertThat(result.get("WK")).isSameAs(parser1);
        assertThat(result.get("US")).isSameAs(parser2);
    }

    @Test
    void parserMap_empty() {
        var result = parserConfig.parserMap(List.of());

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}