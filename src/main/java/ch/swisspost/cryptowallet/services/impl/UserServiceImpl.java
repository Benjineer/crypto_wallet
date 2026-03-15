package ch.swisspost.cryptowallet.services.impl;

import ch.swisspost.cryptowallet.dtos.RegisterRequest;
import ch.swisspost.cryptowallet.entities.User;
import ch.swisspost.cryptowallet.enums.Role;
import ch.swisspost.cryptowallet.exceptions.CryptoWalletClientException;
import ch.swisspost.cryptowallet.repositories.UserRepository;
import ch.swisspost.cryptowallet.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterRequest request) {

        if (request.getRole().equals(Role.ADMIN)) {
            throw new CryptoWalletClientException("Cannot register admin user");
        }

        boolean existsByEmail = userRepository.existsByEmail(request.getEmail());
        if (existsByEmail){
            throw new CryptoWalletClientException("User with email already exists");
        }

        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        return userRepository.save(user);
    }

    @Override
    public User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public void updateUser(String username, User updatedUser) {
        userRepository.findByEmail(username)
                .ifPresentOrElse(user -> {
                    user.setFirstname(updatedUser.getFirstname());
                    user.setLastname(updatedUser.getLastname());
                    user.setEmail(updatedUser.getEmail());
                    userRepository.save(user);
                }, () -> {
                    throw new UsernameNotFoundException("User not found");
                });
    }

    @Override
    public void removeUser(String username) {

        userRepository.findByEmail(username)
                .ifPresent(userRepository::delete);
    }
}
