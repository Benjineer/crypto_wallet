package ch.swisspost.cryptowallet.dtos;

import java.math.BigDecimal;

public record AssetPerformanceDto(
    String symbol,
    BigDecimal quantity,
    BigDecimal currentPrice,
    BigDecimal currentTotalValue,
    BigDecimal performancePercentage
) {}