package ch.swisspost.cryptowallet.dtos;

import java.math.BigDecimal;
import java.util.List;

public record WalletPerformanceResponse(
    BigDecimal totalWalletValue,
    List<AssetPerformanceDto> assets,
    AssetPerformanceDto bestPerformer,
    AssetPerformanceDto worstPerformer
) {}