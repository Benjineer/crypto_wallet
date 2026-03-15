package ch.swisspost.cryptowallet.repositories;

import ch.swisspost.cryptowallet.entities.WalletAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WalletAssetRepository extends JpaRepository<WalletAsset, Long> {

    @Query("SELECT wa FROM WalletAsset wa " +
            "JOIN FETCH wa.asset " +
            "WHERE wa.wallet.userId = :userId")
    List<WalletAsset> findAllByUserIdWithAssets(@Param("userId") String userId);
}
