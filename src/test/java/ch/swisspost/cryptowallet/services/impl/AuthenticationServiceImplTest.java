package ch.swisspost.cryptowallet.services.impl;

import ch.swisspost.cryptowallet.configurations.properties.AuthenticationProperties;
import ch.swisspost.cryptowallet.dtos.AuthenticationRequest;
import ch.swisspost.cryptowallet.dtos.AuthenticationResponse;
import ch.swisspost.cryptowallet.entities.Token;
import ch.swisspost.cryptowallet.entities.User;
import ch.swisspost.cryptowallet.exceptions.CryptoWalletServerException;
import ch.swisspost.cryptowallet.repositories.TokenRepository;
import ch.swisspost.cryptowallet.repositories.UserRepository;
import ch.swisspost.cryptowallet.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private TokenRepository tokenRepository;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private AuthenticationProperties authenticationProperties;

    @InjectMocks private AuthenticationServiceImpl authenticationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
    }

    @Nested
    @DisplayName("authenticate Tests")
    class AuthenticateTests {

        @Test
        @DisplayName("Should return tokens when credentials are valid")
        void authenticate_Success() {
            AuthenticationRequest request = new AuthenticationRequest("test@example.com", "password");
            
            // Mocking logic
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
            when(jwtService.generateToken(user)).thenReturn("access-token");
            when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
            AuthenticationProperties properties = new AuthenticationProperties();
            properties.getJwt().setTokenExpiryTimeMinutes(15);
            properties.getJwt().setRefreshTokenExpiryTimeMinutes(60);
            when(authenticationProperties.getJwt()).thenReturn(properties.getJwt());
            
            // Execute
            AuthenticationResponse response = authenticationService.authenticate(request);

            // Assert
            assertNotNull(response);
            assertEquals("access-token", response.getAccessToken());
            verify(tokenRepository).save(any(Token.class));
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found after auth")
        void authenticate_UserNotFound_ThrowsException() {
            AuthenticationRequest request = new AuthenticationRequest("ghost@example.com", "password");
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class, () -> authenticationService.authenticate(request));
        }

        @Test
        @DisplayName("Should throw exception when authenticationManager fails")
        void authenticate_BadCredentials_ThrowsException() {
            when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

            AuthenticationRequest authenticationRequest = new AuthenticationRequest("a", "b");
            assertThrows(BadCredentialsException.class, 
                () -> authenticationService.authenticate(authenticationRequest));
        }
    }

    @Nested
    @DisplayName("refreshToken Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should generate new access token for valid header")
        void refreshToken_Success() {
            String header = "Bearer valid-refresh";
            when(jwtService.extractUsername("valid-refresh")).thenReturn("test@example.com");
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(jwtService.isTokenValid("valid-refresh", user)).thenReturn(true);
            when(jwtService.generateToken(user)).thenReturn("new-access-token");
            AuthenticationProperties properties = new AuthenticationProperties();
            properties.getJwt().setTokenExpiryTimeMinutes(15);
            properties.getJwt().setRefreshTokenExpiryTimeMinutes(60);
            when(authenticationProperties.getJwt()).thenReturn(properties.getJwt());

            AuthenticationResponse response = authenticationService.refreshToken(header);

            assertEquals("new-access-token", response.getAccessToken());
            assertEquals("valid-refresh", response.getRefreshToken());
            verify(tokenRepository).save(any(Token.class));
        }

        @Test
        @DisplayName("Should throw exception for missing bearer prefix")
        void refreshToken_InvalidHeader_ThrowsException() {
            assertThrows(CryptoWalletServerException.class,
                () -> authenticationService.refreshToken("InvalidHeader"));
        }

        @Test
        @DisplayName("Should throw exception for invalid token signature")
        void refreshToken_InvalidToken_ThrowsException() {
            String header = "Bearer bad-token";
            when(jwtService.extractUsername("bad-token")).thenReturn("test@example.com");
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(jwtService.isTokenValid("bad-token", user)).thenReturn(false);

            assertThrows(CryptoWalletServerException.class, () -> authenticationService.refreshToken(header));
        }
    }
}