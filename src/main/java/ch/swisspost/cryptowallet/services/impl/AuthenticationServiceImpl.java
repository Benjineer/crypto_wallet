package ch.swisspost.cryptowallet.services.impl;

import ch.swisspost.cryptowallet.configurations.properties.AuthenticationProperties;
import ch.swisspost.cryptowallet.dtos.AuthenticationRequest;
import ch.swisspost.cryptowallet.dtos.AuthenticationResponse;
import ch.swisspost.cryptowallet.entities.Token;
import ch.swisspost.cryptowallet.exceptions.CryptoWalletServerException;
import ch.swisspost.cryptowallet.repositories.TokenRepository;
import ch.swisspost.cryptowallet.repositories.UserRepository;
import ch.swisspost.cryptowallet.security.JwtService;
import ch.swisspost.cryptowallet.services.AuthenticationService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationProperties authenticationProperties;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var jwt = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(jwt);

        return AuthenticationResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthenticationResponse refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new CryptoWalletServerException("Missing or invalid Refresh Token header");
        }

        final String refreshJwt = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(refreshJwt);

        var user = this.userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!jwtService.isTokenValid(refreshJwt, user)) {
            throw new CryptoWalletServerException("Invalid refresh token");
        }

        var accessToken = jwtService.generateToken(user);

        // Save the new access token to Redis
        saveUserToken(accessToken);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshJwt)
                .build();
    }

    private void saveUserToken(String jwt) {
        final String jti = jwtService.extractClaim(jwt, Claims::getId);
        final String username = jwtService.extractUsername(jwt);

        Token token = Token.builder()
                .jti(jti)
                .username(username)
                .revoked(false)
                .ttlMinutes(authenticationProperties.getJwt().getTokenExpiryTimeMinutes())
                .build();

        tokenRepository.save(token);
    }
}