package ch.swisspost.cryptowallet.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "wallet_assets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletAsset extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal purchasePrice;

    @Column(nullable = false)
    private OffsetDateTime purchaseDate;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WalletAsset walletAsset = (WalletAsset) o;
        return id != null && Objects.equals(id, walletAsset.id);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}