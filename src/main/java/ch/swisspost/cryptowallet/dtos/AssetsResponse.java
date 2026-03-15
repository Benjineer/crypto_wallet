package ch.swisspost.cryptowallet.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AssetsResponse(
        List<AssetData> data
) {
}