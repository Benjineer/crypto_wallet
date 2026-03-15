package ch.swisspost.cryptowallet.repositories;

import ch.swisspost.cryptowallet.entities.Asset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class AssetRepositoryTest {

    @Autowired
    private AssetRepository assetRepository;

    @Test
    void findBySymbolIgnoreCase_ShouldReturnAsset() {

        assetRepository.save(new Asset("ETH"));

        Optional<Asset> found = assetRepository.findBySymbolIgnoreCase("eth");
        Optional<Asset> foundUpper = assetRepository.findBySymbolIgnoreCase("ETH");

        assertTrue(found.isPresent());
        assertEquals(foundUpper.get().getId(), found.get().getId());
    }

    @Test
    void findAllActive_ShouldExcludeDeletedAssets() {
        Asset btc = new Asset("BTC");
        btc.setDeleted(false);
        Asset xrp = new Asset("XRP");
        xrp.setDeleted(true);
        assetRepository.save(btc);
        assetRepository.save(xrp);

        List<Asset> activeAssets = assetRepository.findAllActive();

        assertEquals(1, activeAssets.size());
        assertEquals("BTC", activeAssets.get(0).getSymbol());
    }
}