package ch.swisspost.cryptowallet.services.impl;

import ch.swisspost.cryptowallet.entities.User;
import ch.swisspost.cryptowallet.enums.Role;
import ch.swisspost.cryptowallet.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Should return UserDetails when user exists in database")
    void loadUserByUsername_Success() {
        // Arrange
        String email = "dev@example.com";
        User userEntity = new User();
        userEntity.setEmail(email);
        userEntity.setPassword("encoded_password");
        userEntity.setRole(Role.USER);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getUsername());
        assertEquals("encoded_password", result.getPassword());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when email is not in DB")
    void loadUserByUsername_UserNotFound_ThrowsException() {
        // Arrange
        String email = "unknown@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, 
            () -> userDetailsService.loadUserByUsername(email));
        
        verify(userRepository, times(1)).findByEmail(email);
    }
}