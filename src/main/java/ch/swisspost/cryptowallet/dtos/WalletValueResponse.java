package ch.swisspost.cryptowallet.dtos;

import java.math.BigDecimal;
import java.util.List;

public record WalletValueResponse(
    BigDecimal totalValue,
    List<AssetValueDto> holdings
) {}

