package ch.swisspost.cryptowallet.services;

import ch.swisspost.cryptowallet.entities.PriceHistory;
import org.springframework.data.domain.Page;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface PriceService {

    Optional<PriceHistory> getLatestPrice(Long assetId);

    List<PriceHistory> saveAllPrices(List<PriceHistory> priceHistories);

    Optional<PriceHistory> findPriceAtDate(Long assetId, OffsetDateTime endOfDay);

    Page<PriceHistory> getPriceHistory(String symbol, int page, int size);
}
