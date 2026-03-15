package ch.swisspost.cryptowallet.repositories;

import ch.swisspost.cryptowallet.entities.User;
import ch.swisspost.cryptowallet.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByEmail_ShouldReturnTrueIfPresent() {
        String email = "test@example.com";
        userRepository.save(User.builder().firstname("firstname").lastname("lastname").email(email)
                .password("hashed").role(Role.USER).build());

        assertTrue(userRepository.existsByEmail(email));
        assertFalse(userRepository.existsByEmail("other@example.com"));
    }

    @Test
    void findByEmail_ShouldReturnUser() {
        String email = "find@me.com";
        userRepository.save(User.builder().firstname("firstname").lastname("lastname").email(email).role(Role.USER).build());

        Optional<User> found = userRepository.findByEmail(email);
        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail());
    }
}