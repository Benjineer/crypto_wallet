package ch.swisspost.cryptowallet.repositories;

import ch.swisspost.cryptowallet.entities.Asset;
import ch.swisspost.cryptowallet.entities.Wallet;
import ch.swisspost.cryptowallet.entities.WalletAsset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class WalletAssetRepositoryTest {

    @Autowired
    private WalletAssetRepository walletAssetRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private AssetRepository assetRepository;

    @Test
    void findAllByUserIdWithAssets_ShouldFetchAssetsSuccessfully() {
        String userId = "owner-1";
        Wallet wallet = walletRepository.save(Wallet.builder().userId(userId).build());
        Asset btc = assetRepository.save(Asset.builder().symbol("BTC").build());
        
        walletAssetRepository.save(WalletAsset.builder()
                .wallet(wallet)
                .asset(btc)
                .quantity(BigDecimal.ONE)
                .purchaseDate(OffsetDateTime.now())
                .purchasePrice(BigDecimal.TEN)
                .build());

        List<WalletAsset> results = walletAssetRepository.findAllByUserIdWithAssets(userId);

        assertEquals(1, results.size());
        assertEquals("BTC", results.get(0).getAsset().getSymbol());
        assertNotNull(results.get(0).getAsset().getId());
    }
}