package ch.swisspost.cryptowallet.services.impl;

import ch.swisspost.cryptowallet.dtos.WalletPerformanceResponse;
import ch.swisspost.cryptowallet.dtos.WalletResponse;
import ch.swisspost.cryptowallet.entities.Asset;
import ch.swisspost.cryptowallet.entities.PriceHistory;
import ch.swisspost.cryptowallet.entities.Wallet;
import ch.swisspost.cryptowallet.entities.WalletAsset;
import ch.swisspost.cryptowallet.exceptions.CryptoWalletClientException;
import ch.swisspost.cryptowallet.repositories.AssetRepository;
import ch.swisspost.cryptowallet.repositories.WalletAssetRepository;
import ch.swisspost.cryptowallet.repositories.WalletRepository;
import ch.swisspost.cryptowallet.services.PriceService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private WalletAssetRepository walletAssetRepository;
    @Mock
    private PriceService priceService;
    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Nested
    @DisplayName("createWallet Tests")
    class CreateWalletTests {

        @Test
        @DisplayName("Should throw Exception when user already has a wallet")
        void createWallet_UserExists_ThrowsException() {
            String userId = "user123";
            when(walletRepository.existsByUserId(userId)).thenReturn(true);

            assertThrows(CryptoWalletClientException.class, () -> walletService.createWallet(userId));
            verify(walletRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create wallet successfully")
        void createWallet_Success() {
            String userId = "user123";
            Wallet wallet = new Wallet();
            wallet.setId(1L);
            wallet.setUserId(userId);

            when(walletRepository.existsByUserId(userId)).thenReturn(false);
            when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

            WalletResponse response = walletService.createWallet(userId);

            assertNotNull(response);
            assertEquals(userId, response.userId());
            verify(walletRepository).save(any());
        }
    }

    @Nested
    @DisplayName("buyAsset Tests")
    class BuyAssetTests {

        @Test
        @DisplayName("Should throw EntityNotFound when wallet doesn't exist")
        void buyAsset_NoWallet_ThrowsException() {
            when(walletRepository.findByUserId("user1")).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> walletService.buyAsset("user1", "BTC", BigDecimal.ONE));
        }

        @Test
        @DisplayName("Should throw EntityNotFound when asset is not supported")
        void buyAsset_InvalidAsset_ThrowsException() {
            when(walletRepository.findByUserId("user1")).thenReturn(Optional.of(new Wallet()));
            when(assetRepository.findBySymbolIgnoreCase("SHIB")).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> walletService.buyAsset("user1", "SHIB", BigDecimal.ONE));
        }

        @Test
        @DisplayName("Should throw CryptoException when price data is missing")
        void buyAsset_NoPrice_ThrowsException() {
            Asset btc = new Asset();
            btc.setId(1L);
            when(walletRepository.findByUserId("user1")).thenReturn(Optional.of(new Wallet()));
            when(assetRepository.findBySymbolIgnoreCase("BTC")).thenReturn(Optional.of(btc));
            when(priceService.getLatestPrice(1L)).thenReturn(Optional.empty());

            assertThrows(CryptoWalletClientException.class,
                    () -> walletService.buyAsset("user1", "BTC", BigDecimal.ONE));
        }

        @Test
        @DisplayName("Should successfully save holding when all data is valid")
        void buyAsset_Success() {
            // Arrange
            String userId = "user1";
            String symbol = "BTC";
            BigDecimal quantity = BigDecimal.valueOf(0.5);
            BigDecimal price = BigDecimal.valueOf(50000);

            Wallet wallet = new Wallet();
            Asset asset = new Asset();
            asset.setId(1L);
            asset.setSymbol(symbol);

            PriceHistory priceHistory = new PriceHistory();
            priceHistory.setPrice(price);

            when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
            when(assetRepository.findBySymbolIgnoreCase(symbol)).thenReturn(Optional.of(asset));
            when(priceService.getLatestPrice(1L)).thenReturn(Optional.of(priceHistory));

            // Act
            walletService.buyAsset(userId, symbol, quantity);

            // Assert
            verify(walletAssetRepository, times(1)).save(argThat(holding ->
                    holding.getWallet().equals(wallet) &&
                            holding.getAsset().equals(asset) &&
                            holding.getQuantity().equals(quantity) &&
                            holding.getPurchasePrice().equals(price)
            ));
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should return empty response when user has no holdings")
        void getPerformance_NoHoldings_ReturnsEmpty() {
            when(walletAssetRepository.findAllByUserIdWithAssets("user1")).thenReturn(List.of());

            WalletPerformanceResponse response = walletService.getPerformance("user1");

            assertEquals(BigDecimal.ZERO, response.totalWalletValue());
            assertTrue(response.assets().isEmpty());
            assertNull(response.bestPerformer());
        }

        @Test
        @DisplayName("Should handle performance calculation with zero cost")
        void getPerformance_ZeroCost_HandlesDivision() {
            Asset btc = new Asset();
            btc.setId(1L);
            btc.setSymbol("BTC");
            WalletAsset holding = new WalletAsset();
            holding.setAsset(btc);
            holding.setQuantity(BigDecimal.ONE);
            holding.setPurchasePrice(BigDecimal.ZERO); // Zero cost case

            when(walletAssetRepository.findAllByUserIdWithAssets("user1")).thenReturn(List.of(holding));
            when(priceService.getLatestPrice(1L)).thenReturn(Optional.of(new PriceHistory(btc, BigDecimal.TEN, Clock.systemUTC().millis())));

            WalletPerformanceResponse response = walletService.getPerformance("user1");

            // Should not crash with ArithmeticException (division by zero)
            assertNotNull(response);
            assertEquals(BigDecimal.ZERO.setScale(2), response.assets().get(0).performancePercentage());
        }
    }
}