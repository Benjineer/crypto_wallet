package ch.swisspost.cryptowallet.configurations;

import io.jsonwebtoken.Jwts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;

@Configuration
public class JwtConfig {

    @Bean
    public KeyPair jwtKeyPair() {
        return Jwts.SIG.RS512.keyPair().build();
    }
}