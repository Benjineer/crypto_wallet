package ch.swisspost.cryptowallet.dtos;

import java.math.BigDecimal;

public record AssetValueDto(
    String symbol,
    BigDecimal quantity,
    BigDecimal currentPrice,
    BigDecimal totalValue
) {}