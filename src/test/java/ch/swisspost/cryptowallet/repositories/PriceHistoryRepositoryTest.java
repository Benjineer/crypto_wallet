package ch.swisspost.cryptowallet.repositories;

import ch.swisspost.cryptowallet.entities.Asset;
import ch.swisspost.cryptowallet.entities.PriceHistory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class PriceHistoryRepositoryTest {

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;
    
    @Autowired
    private AssetRepository assetRepository;

    @Test
    void findPriceAtDate_ShouldReturnCorrectHistoricalPrice() {
        Asset btc = assetRepository.save(new Asset("BTC"));
        OffsetDateTime now = OffsetDateTime.now();

        // Save prices: one far past, one just before target, one in future
        priceHistoryRepository.save(new PriceHistory(btc, new BigDecimal("40000"), now.minusDays(5).toInstant().toEpochMilli()));
        priceHistoryRepository.save(new PriceHistory(btc, new BigDecimal("50000"), now.minusHours(1).toInstant().toEpochMilli()));
        priceHistoryRepository.save(new PriceHistory(btc, new BigDecimal("60000"), now.plusHours(1).toInstant().toEpochMilli()));

        Optional<PriceHistory> result = priceHistoryRepository.findPriceAtDate(btc.getId(), now);

        assertTrue(result.isPresent());
        assertEquals(0, new BigDecimal("50000").compareTo(result.get().getPrice()));
    }

    @Test
    void findTopByAssetIdOrderByPriceTimestampDesc_ShouldReturnLatest() {
        Asset btc = assetRepository.save(new Asset("BTC"));
        
        priceHistoryRepository.save(new PriceHistory(btc, new BigDecimal("10"), OffsetDateTime.now().minusDays(1).toInstant().toEpochMilli()));
        priceHistoryRepository.save(new PriceHistory(btc, new BigDecimal("20"), OffsetDateTime.now().toInstant().toEpochMilli()));

        Optional<PriceHistory> latest = priceHistoryRepository.findTopByAssetIdOrderByPriceTimestampDesc(btc.getId());

        assertTrue(latest.isPresent());
        assertEquals(0, new BigDecimal("20").compareTo(latest.get().getPrice()));
    }
}