package ch.swisspost.cryptowallet.services;

import ch.swisspost.cryptowallet.dtos.WalletPerformanceResponse;
import ch.swisspost.cryptowallet.dtos.WalletResponse;
import ch.swisspost.cryptowallet.dtos.WalletValueResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface WalletService {

    WalletResponse createWallet(String userId);

    void buyAsset(String userId, String symbol, BigDecimal quantity);

    WalletValueResponse getCurrentWalletValue(String userId);

    BigDecimal getHistoricalValue(String userId, LocalDate date);

    WalletPerformanceResponse getPerformance(String userId);
}
