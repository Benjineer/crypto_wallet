package ch.swisspost.cryptowallet.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;


@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@Table(name = "assets")
public class Asset extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String symbol;

    public Asset(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Asset asset = (Asset) o;
        return id != null && Objects.equals(id, asset.id);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}