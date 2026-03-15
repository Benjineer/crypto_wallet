package ch.swisspost.cryptowallet.services.impl;

import ch.swisspost.cryptowallet.entities.PriceHistory;
import ch.swisspost.cryptowallet.repositories.PriceHistoryRepository;
import ch.swisspost.cryptowallet.services.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PriceServiceImpl implements PriceService {

    private final PriceHistoryRepository priceHistoryRepository;

    @Override
    @Cacheable(value = "latestPrices", key = "#asset.id")
    public Optional<PriceHistory> getLatestPrice(Long assetId) {
        return priceHistoryRepository.findTopByAssetIdOrderByPriceTimestampDesc(assetId);
    }

    @Override
    @CacheEvict(value = "latestPrices", allEntries = true)
    public List<PriceHistory> saveAllPrices(List<PriceHistory> priceHistories) {
        return priceHistoryRepository.saveAll(priceHistories);
    }

    @Override
    public Optional<PriceHistory> findPriceAtDate(Long assetId, OffsetDateTime endOfDay) {
        return priceHistoryRepository.findPriceAtDate(assetId, endOfDay);
    }

    @Override
    public Page<PriceHistory> getPriceHistory(String assetSymbol, int page, int size) {
        return priceHistoryRepository.findByAssetSymbolOrderByPriceTimestampDesc(assetSymbol, PageRequest.of(page, size));
    }
}