package ch.swisspost.cryptowallet.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BuyRequest(
    @NotBlank String symbol,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity
) {}