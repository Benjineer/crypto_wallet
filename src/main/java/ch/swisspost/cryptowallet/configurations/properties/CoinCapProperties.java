package ch.swisspost.cryptowallet.configurations.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "coincap")
public class CoinCapProperties {

    @NotBlank
    private String baseUrl = "https://rest.coincap.io/v3";

    @NotBlank
    private String apiKey;

    @NotBlank
    private String assetsPath = "/assets";

    @NotBlank
    private String pricePath = "/price/bysymbol/{symbol}";
}
