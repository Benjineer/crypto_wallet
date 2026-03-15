package ch.swisspost.cryptowallet.configurations.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "jobs.scheduling")
public class SchedulingProperties {

    @NotBlank
    private String fetchAssetsCron = "*/60 * * * * *"; // Defaults to midnight

    @NotBlank
    private String fetchPriceCron = "0/30 * * * * *"; // Defaults to 30s
}