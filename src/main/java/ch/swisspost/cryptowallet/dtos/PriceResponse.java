package ch.swisspost.cryptowallet.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PriceResponse(
    Long timestamp,
    List<String> data
) {}