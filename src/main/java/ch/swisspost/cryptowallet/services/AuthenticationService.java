package ch.swisspost.cryptowallet.services;

import ch.swisspost.cryptowallet.dtos.AuthenticationRequest;
import ch.swisspost.cryptowallet.dtos.AuthenticationResponse;

public interface AuthenticationService {

    AuthenticationResponse authenticate(AuthenticationRequest request);

    AuthenticationResponse refreshToken(String authHeader);
}
