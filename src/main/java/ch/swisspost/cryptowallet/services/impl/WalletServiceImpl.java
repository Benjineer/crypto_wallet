package ch.swisspost.cryptowallet.services.impl;

import ch.swisspost.cryptowallet.dtos.*;
import ch.swisspost.cryptowallet.entities.Asset;
import ch.swisspost.cryptowallet.entities.PriceHistory;
import ch.swisspost.cryptowallet.entities.Wallet;
import ch.swisspost.cryptowallet.entities.WalletAsset;
import ch.swisspost.cryptowallet.exceptions.CryptoWalletClientException;
import ch.swisspost.cryptowallet.repositories.AssetRepository;
import ch.swisspost.cryptowallet.repositories.WalletAssetRepository;
import ch.swisspost.cryptowallet.repositories.WalletRepository;
import ch.swisspost.cryptowallet.services.PriceService;
import ch.swisspost.cryptowallet.services.WalletService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletAssetRepository walletAssetRepository; // New dependency
    private final PriceService priceService;
    private final AssetRepository assetRepository;

    @Override
    @Transactional
    public WalletResponse createWallet(String userId) {
        if (walletRepository.existsByUserId(userId)) {
            throw new CryptoWalletClientException("User already has a wallet");
        }
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        Wallet saved = walletRepository.save(wallet);
        return new WalletResponse(saved.getId(), saved.getUserId());
    }

    @Override
    @Transactional
    public void buyAsset(String userId, String symbol, BigDecimal quantity) {

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User does not have a wallet. Create one first."));

        Asset asset = assetRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new EntityNotFoundException("Asset not supported: " + symbol));

        BigDecimal acquisitionPrice = priceService.getLatestPrice(asset.getId())
                .map(PriceHistory::getPrice)
                .orElseThrow(() -> new CryptoWalletClientException("Price data currently unavailable"));

        WalletAsset holding = new WalletAsset();
        holding.setWallet(wallet);
        holding.setAsset(asset);
        holding.setQuantity(quantity);
        holding.setPurchasePrice(acquisitionPrice);
        holding.setPurchaseDate(OffsetDateTime.now());

        walletAssetRepository.save(holding);
    }

    @Override
    public WalletValueResponse getCurrentWalletValue(String userId) {

        List<WalletAsset> holdings = walletAssetRepository.findAllByUserIdWithAssets(userId);

        Map<Asset, BigDecimal> aggregatedQuantities = holdings.stream()
                .collect(Collectors.groupingBy(
                        WalletAsset::getAsset,
                        Collectors.reducing(BigDecimal.ZERO, WalletAsset::getQuantity, BigDecimal::add)
                ));

        List<AssetValueDto> assetValues = aggregatedQuantities.entrySet().stream()
                .map(entry -> {
                    Asset asset = entry.getKey();
                    BigDecimal totalQuantity = entry.getValue();

                    BigDecimal currentPrice = priceService.getLatestPrice(asset.getId())
                            .map(PriceHistory::getPrice)
                            .orElse(BigDecimal.ZERO);

                    return new AssetValueDto(
                            asset.getSymbol(),
                            totalQuantity,
                            currentPrice,
                            currentPrice.multiply(totalQuantity).setScale(2, RoundingMode.HALF_UP)
                    );
                }).toList();

        BigDecimal totalValue = assetValues.stream()
                .map(AssetValueDto::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new WalletValueResponse(totalValue, assetValues);
    }

    @Override
    public BigDecimal getHistoricalValue(String userId, LocalDate date) {

        OffsetDateTime endOfDay = date.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        List<WalletAsset> holdings = walletAssetRepository.findAllByUserIdWithAssets(userId);

        return holdings.stream()
                .map(holding -> {
                    BigDecimal priceAtDate = priceService.findPriceAtDate(holding.getAsset().getId(), endOfDay)
                            .map(PriceHistory::getPrice)
                            .orElse(holding.getPurchasePrice());
                    return holding.getQuantity().multiply(priceAtDate);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public WalletPerformanceResponse getPerformance(String userId) {

        List<WalletAsset> holdings = walletAssetRepository.findAllByUserIdWithAssets(userId);

        if (holdings.isEmpty()) {
            return new WalletPerformanceResponse(BigDecimal.ZERO, List.of(), null, null);
        }

        List<AssetPerformanceDto> aggregatedPerformances = holdings.stream()
                .collect(Collectors.groupingBy(WalletAsset::getAsset))
                .entrySet().stream()
                .map(entry -> {
                    Asset asset = entry.getKey();
                    List<WalletAsset> assetHoldings = entry.getValue();

                    BigDecimal totalQuantity = assetHoldings.stream()
                            .map(WalletAsset::getQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalCost = assetHoldings.stream()
                            .map(h -> h.getQuantity().multiply(h.getPurchasePrice()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal currentPrice = priceService.getLatestPrice(asset.getId())
                            .map(PriceHistory::getPrice)
                            .orElse(BigDecimal.ZERO);

                    BigDecimal currentTotalValue = currentPrice.multiply(totalQuantity);

                    BigDecimal performancePct = BigDecimal.ZERO;
                    if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
                        performancePct = currentTotalValue.subtract(totalCost)
                                .divide(totalCost, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                    }

                    return new AssetPerformanceDto(
                            asset.getSymbol(),
                            totalQuantity,
                            currentPrice,
                            currentTotalValue.setScale(2, RoundingMode.HALF_UP),
                            performancePct.setScale(2, RoundingMode.HALF_UP)
                    );
                }).toList();

        BigDecimal totalWalletValue = aggregatedPerformances.stream()
                .map(AssetPerformanceDto::currentTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        AssetPerformanceDto best = aggregatedPerformances.stream()
                .max(Comparator.comparing(AssetPerformanceDto::performancePercentage))
                .orElse(null);

        AssetPerformanceDto worst = aggregatedPerformances.stream()
                .min(Comparator.comparing(AssetPerformanceDto::performancePercentage))
                .orElse(null);

        return new WalletPerformanceResponse(
                totalWalletValue.setScale(2, RoundingMode.HALF_UP),
                aggregatedPerformances,
                best,
                worst
        );
    }
}