package ch.swisspost.cryptowallet.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity
@Table(name = "price_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal price;

    @Column(nullable = false)
    private OffsetDateTime priceTimestamp;

    public PriceHistory(Asset asset, BigDecimal price, Long timestamp) {
        this.asset = asset;
        this.price = price;
        this.priceTimestamp = OffsetDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PriceHistory priceHistory = (PriceHistory) o;
        return id != null && Objects.equals(id, priceHistory.id);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}