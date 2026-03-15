/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.swisspost.cryptowallet.schedulers;

import ch.swisspost.cryptowallet.configurations.properties.CoinCapProperties;
import ch.swisspost.cryptowallet.dtos.AssetData;
import ch.swisspost.cryptowallet.dtos.AssetsResponse;
import ch.swisspost.cryptowallet.dtos.PriceResponse;
import ch.swisspost.cryptowallet.entities.Asset;
import ch.swisspost.cryptowallet.entities.PriceHistory;
import ch.swisspost.cryptowallet.exceptions.CryptoWalletServerException;
import ch.swisspost.cryptowallet.repositories.AssetRepository;
import ch.swisspost.cryptowallet.services.PriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Oke
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetPriceSchedular {

    private final CoinCapProperties coinCapProperties;
    private final WebClient webClient;
    private final AssetRepository assetRepository;
    private final PriceService priceService;

    @SchedulerLock(name = "fetchAssetsLock", lockAtMostFor = "10m", lockAtLeastFor = "1m")
    @Scheduled(cron = "#{@schedulingProperties.fetchAssetsCron}")
    public void syncAssets() {
        log.warn("Starting to Sync asserts");
        webClient.get()
                .uri(coinCapProperties.getAssetsPath())
                .retrieve()
                .bodyToMono(AssetsResponse.class)
                .timeout(Duration.ofSeconds(10))
                .flatMap(res -> {
                    if (res == null || res.data() == null) {
                        return Mono.error(new CryptoWalletServerException("Assets API returned empty asset data"));
                    }
                    return Mono.just(res.data().stream()
                            .map(AssetData::symbol)
                            .collect(Collectors.toSet()));
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(5)))
                .publishOn(Schedulers.boundedElastic())
                .flatMap(apiSymbols -> {
                    Set<String> existingSymbols = assetRepository.findAll()
                            .stream()
                            .map(Asset::getSymbol)
                            .collect(Collectors.toSet());

                    List<Asset> newAssets = apiSymbols.stream()
                            .filter(symbol -> !existingSymbols.contains(symbol))
                            .map(Asset::new)
                            .toList();

                    if (!newAssets.isEmpty()) {
                        assetRepository.saveAll(newAssets);
                        log.info("Saved {} new assets to database", newAssets.size());
                    }

                    return Mono.just(newAssets.size());
                })
                .subscribe(
                        count -> log.info("Assets API Sync complete."),
                        error -> log.error("Assets API Sync failed: {}", error.getMessage())
                );
    }

    @SchedulerLock(name = "fetchPriceLock", lockAtMostFor = "5m", lockAtLeastFor = "30s")
    @Scheduled(cron = "#{@schedulingProperties.fetchPriceCron}")
    public void updatePrices() {

        List<Asset> assets = assetRepository.findAll();

        if (assets.isEmpty()) {
            log.warn("No assets found in DB/Cache. Skipping price update.");
            return;
        }

        Flux.fromIterable(assets)
                .flatMap(this::fetchPriceForAsset, 3)
                .collectList()
                .filter(list -> !list.isEmpty())
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(priceService::saveAllPrices)
                .subscribe(
                        results -> log.info("Successfully persisted {} prices to history", results.size()),
                        error -> log.error("Batch price update failed", error)
                );
    }

    private Mono<PriceHistory> fetchPriceForAsset(Asset asset) {
        return webClient.get()
                .uri(coinCapProperties.getPricePath(), asset.getSymbol())
                .retrieve()
                .bodyToMono(PriceResponse.class)
                .timeout(Duration.ofSeconds(5))
                .flatMap(res -> {
                    if (res == null || res.data() == null || res.data().isEmpty()) {
                        return Mono.empty();
                    }
                    try {
                        BigDecimal price = new BigDecimal(res.data().getFirst());
                        return Mono.just(new PriceHistory(asset, price, res.timestamp()));
                    } catch (Exception e) {
                        log.error("Parsing error for {}: {}", asset.getSymbol(), e.getMessage());
                        return Mono.empty();
                    }
                })
                .onErrorResume(e -> {
                    log.warn("Could not fetch price for {}: {}", asset.getSymbol(), e.getMessage());
                    return Mono.empty();
                });
    }
}
