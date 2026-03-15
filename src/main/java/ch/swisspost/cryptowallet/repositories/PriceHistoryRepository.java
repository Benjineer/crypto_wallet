package ch.swisspost.cryptowallet.repositories;

import ch.swisspost.cryptowallet.entities.PriceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    Page<PriceHistory> findByAssetSymbolOrderByPriceTimestampDesc(String symbol, Pageable pageable);

    Optional<PriceHistory> findTopByAssetIdOrderByPriceTimestampDesc(Long assetId);

    @Query(value = """
                SELECT * FROM price_history 
                WHERE asset_id = :assetId 
                  AND price_timestamp <= :endOfDay 
                ORDER BY price_timestamp DESC 
                LIMIT 1
            """, nativeQuery = true)
    Optional<PriceHistory> findPriceAtDate(@Param("assetId") Long assetId,
                                           @Param("endOfDay") OffsetDateTime endOfDay);
}