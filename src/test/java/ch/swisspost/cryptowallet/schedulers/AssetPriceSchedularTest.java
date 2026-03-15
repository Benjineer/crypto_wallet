package ch.swisspost.cryptowallet.schedulers;

import ch.swisspost.cryptowallet.configurations.properties.CoinCapProperties;
import ch.swisspost.cryptowallet.dtos.AssetData;
import ch.swisspost.cryptowallet.dtos.AssetsResponse;
import ch.swisspost.cryptowallet.dtos.PriceResponse;
import ch.swisspost.cryptowallet.entities.Asset;
import ch.swisspost.cryptowallet.entities.PriceHistory;
import ch.swisspost.cryptowallet.repositories.AssetRepository;
import ch.swisspost.cryptowallet.services.PriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetPriceSchedularTest {

    @Mock private AssetRepository assetRepository;
    @Mock private PriceService priceService;
    @Mock private CoinCapProperties coinCapProperties;
    @Mock private WebClient webClient;

    @Mock private WebClient.RequestHeadersUriSpec uriSpec;
    @Mock private WebClient.RequestHeadersSpec headersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AssetPriceSchedular schedular;

    @Captor
    private ArgumentCaptor<List<Asset>> assetListCaptor;

    @Captor
    private ArgumentCaptor<List<PriceHistory>> priceHistoryCaptor;

    @BeforeEach
    void setUp() {
        lenient().when(webClient.get()).thenReturn(uriSpec);
        lenient().when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        lenient().when(uriSpec.uri(anyString(), any(Object[].class))).thenReturn(headersSpec);
        lenient().when(headersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("syncAssets: Positive - Capture and verify only new assets are saved")
    void syncAssets() {
        // Arrange
        when(coinCapProperties.getAssetsPath()).thenReturn("/assets");
        AssetsResponse mockRes = new AssetsResponse(List.of(
                new AssetData("BTC"),
                new AssetData("SOL"),
                new AssetData("DOT")
        ));
        when(responseSpec.bodyToMono(AssetsResponse.class)).thenReturn(Mono.just(mockRes));

        // BTC already exists in DB
        when(assetRepository.findAll()).thenReturn(List.of(new Asset("BTC")));

        // Act
        schedular.syncAssets();

        // Assert
        verify(assetRepository, timeout(1000)).saveAll(assetListCaptor.capture());

        List<Asset> savedAssets = assetListCaptor.getValue();
        assertEquals(2, savedAssets.size());
        assertTrue(savedAssets.stream().anyMatch(a -> a.getSymbol().equals("SOL")));
        assertTrue(savedAssets.stream().anyMatch(a -> a.getSymbol().equals("DOT")));
    }

    @Test
    @DisplayName("updatePrices: Positive - Verify successful parsing and batch saving")
    void updatePrices() {
        // Arrange
        Asset btc = new Asset("BTC");
        when(assetRepository.findAll()).thenReturn(List.of(btc));
        when(coinCapProperties.getPricePath()).thenReturn("/price/{s}");

        PriceResponse btcPrice = new PriceResponse( 1678901234L, List.of("50000.00"));
        when(responseSpec.bodyToMono(PriceResponse.class)).thenReturn(Mono.just(btcPrice));

        // Act
        schedular.updatePrices();

        // Assert
        verify(priceService, timeout(1000)).saveAllPrices(priceHistoryCaptor.capture());

        List<PriceHistory> savedHistory = priceHistoryCaptor.getValue();
        assertEquals(1, savedHistory.size());
        assertEquals(0, new BigDecimal("50000.00").compareTo(savedHistory.get(0).getPrice()));
        assertEquals(btc, savedHistory.get(0).getAsset());
    }
}