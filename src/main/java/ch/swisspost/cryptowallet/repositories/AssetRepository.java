package ch.swisspost.cryptowallet.repositories;

import ch.swisspost.cryptowallet.entities.Asset;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

  Optional<Asset> findBySymbolIgnoreCase(String symbol);

  @Cacheable("activeAssets")
  @Query("SELECT a FROM Asset a WHERE a.isDeleted = false")
  List<Asset> findAllActive();

  @CacheEvict(value = "activeAssets", allEntries = true)
  <S extends Asset> List<S> saveAll(Iterable<S> entities);
}
