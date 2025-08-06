package intrum.payoutingestion.configuration;

import static java.util.stream.Collectors.toMap;

import intrum.payoutingestion.parsers.CountryPayoutParser;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParserConfig {

    @Bean
    public Map<String, CountryPayoutParser> parserMap(List<CountryPayoutParser> list) {
        return list.stream().collect(toMap(CountryPayoutParser::countryCode, p -> p));
    }
}
