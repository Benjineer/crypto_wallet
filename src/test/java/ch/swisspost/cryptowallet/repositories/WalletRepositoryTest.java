package ch.swisspost.cryptowallet.repositories;

import ch.swisspost.cryptowallet.entities.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class WalletRepositoryTest {

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void findByUserId_ShouldReturnWallet() {
        String userId = "user-123";
        walletRepository.save(Wallet.builder().userId(userId).build());

        Optional<Wallet> wallet = walletRepository.findByUserId(userId);

        assertTrue(wallet.isPresent());
        assertEquals(userId, wallet.get().getUserId());
    }
}