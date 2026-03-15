package ch.swisspost.cryptowallet.configurations.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "authentication")
public class AuthenticationProperties {

    private JwtProperties jwt = new JwtProperties();

    @Data
    public static class JwtProperties {

        private Integer tokenExpiryTimeMinutes = 30;

        private Integer refreshTokenExpiryTimeMinutes = 1440;
    }
}
