package ch.swisspost.cryptowallet.repositories;

import ch.swisspost.cryptowallet.entities.Token;
import org.springframework.data.repository.CrudRepository;

public interface TokenRepository extends CrudRepository<Token, String> {
}
