package ch.swisspost.cryptowallet.repositories;

import ch.swisspost.cryptowallet.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(String userId);

    boolean existsByUserId(String userId);
}