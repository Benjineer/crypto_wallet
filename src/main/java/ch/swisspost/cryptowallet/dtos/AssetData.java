package ch.swisspost.cryptowallet.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AssetData(
    String symbol
) {}