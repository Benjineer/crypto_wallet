package ch.swisspost.cryptowallet.services;

import ch.swisspost.cryptowallet.dtos.RegisterRequest;
import ch.swisspost.cryptowallet.entities.User;

public interface UserService {

    User register(RegisterRequest request);

    User getUser(String email);

    void updateUser(String username, User updatedUser);

    void removeUser(String username);
}
