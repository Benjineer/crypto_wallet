package ch.swisspost.cryptowallet.services.impl;

import ch.swisspost.cryptowallet.entities.PriceHistory;
import ch.swisspost.cryptowallet.repositories.PriceHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceServiceImplTest {

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @InjectMocks
    private PriceServiceImpl priceService;

    private PriceHistory samplePrice;

    @BeforeEach
    void setUp() {
        samplePrice = new PriceHistory();
        samplePrice.setId(1L);
        // Set other fields as needed for your entity
    }

    @Test
    void getLatestPrice_ShouldReturnPrice_WhenAssetExists() {
        // Arrange
        Long assetId = 1L;
        when(priceHistoryRepository.findTopByAssetIdOrderByPriceTimestampDesc(assetId))
                .thenReturn(Optional.of(samplePrice));

        // Act
        Optional<PriceHistory> result = priceService.getLatestPrice(assetId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(samplePrice.getId(), result.get().getId());
        verify(priceHistoryRepository, times(1)).findTopByAssetIdOrderByPriceTimestampDesc(assetId);
    }

    @Test
    void saveAllPrices_ShouldReturnSavedList() {
        // Arrange
        List<PriceHistory> prices = Arrays.asList(samplePrice);
        when(priceHistoryRepository.saveAll(prices)).thenReturn(prices);

        // Act
        List<PriceHistory> result = priceService.saveAllPrices(prices);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(priceHistoryRepository, times(1)).saveAll(prices);
    }

    @Test
    void findPriceAtDate_ShouldCallRepository() {
        // Arrange
        Long assetId = 1L;
        OffsetDateTime now = OffsetDateTime.now();
        when(priceHistoryRepository.findPriceAtDate(assetId, now)).thenReturn(Optional.of(samplePrice));

        // Act
        Optional<PriceHistory> result = priceService.findPriceAtDate(assetId, now);

        // Assert
        assertTrue(result.isPresent());
        verify(priceHistoryRepository).findPriceAtDate(assetId, now);
    }

    @Test
    void getPriceHistory_ShouldReturnPageOfPrices() {
        // Arrange
        String symbol = "BTC";
        int page = 0;
        int size = 10;
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<PriceHistory> pricePage = new PageImpl<>(Arrays.asList(samplePrice));

        when(priceHistoryRepository.findByAssetSymbolOrderByPriceTimestampDesc(symbol, pageRequest))
                .thenReturn(pricePage);

        // Act
        Page<PriceHistory> result = priceService.getPriceHistory(symbol, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(priceHistoryRepository).findByAssetSymbolOrderByPriceTimestampDesc(symbol, pageRequest);
    }
}