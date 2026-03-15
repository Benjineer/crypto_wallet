package ch.swisspost.cryptowallet.repositories;

import ch.swisspost.cryptowallet.configuration.TestRedisConfiguration;
import ch.swisspost.cryptowallet.entities.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@DataRedisTest
@Import(TestRedisConfiguration.class)
class TokenRepositoryTest {

    @Autowired
    private TokenRepository tokenRepository;

    @Test
    @DisplayName("Token: Save and Retrieve from Redis")
    void token_SaveAndFind() {
        Token token = Token.builder()
                .jti("uuid-123")
                .username("dev_user")
                .revoked(false)
                .ttlMinutes(10)
                .build();

        tokenRepository.save(token);

        Optional<Token> savedToken = tokenRepository.findById("uuid-123");

        assertTrue(savedToken.isPresent());
        assertEquals("dev_user", savedToken.get().getUsername());
    }

    @Test
    @DisplayName("Token should expire and be removed from Redis after TTL")
    void token_ShouldExpire() {
        // Arrange
        String jti = "expiring-jti";
        Token token = Token.builder()
                .jti(jti)
                .username("expiring_user")
                .ttlMinutes(1)
                .build();

        // Act
        tokenRepository.save(token);

        // Verify it exists initially
        assertTrue(tokenRepository.findById(jti).isPresent(), "Token should exist initially");

        await()
                .atMost(61, SECONDS)
                .untilAsserted(() -> {
                    // Assert
                    Optional<Token> expiredToken = tokenRepository.findById(jti);
                    assertFalse(expiredToken.isPresent(), "Token should have expired and been removed");
                });
    }
}