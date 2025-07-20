package com.example.UrlShortner.repository;

import com.example.UrlShortner.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();
    }

    @Test
    void findByUsername_ExistingUser_ReturnsUser() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        User foundUser = userRepository.findByUsername("testuser");

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("testuser");
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.getUserKey()).isEqualTo(savedUser.getUserKey());
    }

    @Test
    void findByUsername_NonExistingUser_ReturnsNull() {
        // When
        User foundUser = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(foundUser).isNull();
    }

    @Test
    void findByEmail_ExistingUser_ReturnsUser() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getUserKey()).isEqualTo(savedUser.getUserKey());
    }

    @Test
    void findByEmail_NonExistingUser_ReturnsEmpty() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByUserKey_ExistingUser_ReturnsUser() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        User foundUser = userRepository.findByUserKey(savedUser.getUserKey());

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUserKey()).isEqualTo(savedUser.getUserKey());
        assertThat(foundUser.getUsername()).isEqualTo("testuser");
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByUserKey_NonExistingUser_ReturnsNull() {
        // When
        User foundUser = userRepository.findByUserKey("non-existent-key");

        // Then
        assertThat(foundUser).isNull();
    }

    @Test
    void existsByUsername_ExistingUser_ReturnsTrue() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByUsername("testuser");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_NonExistingUser_ReturnsFalse() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_ExistingUser_ReturnsTrue() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_NonExistingUser_ReturnsFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void save_NewUser_CreatesUserWithGeneratedKey() {
        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertThat(savedUser.getUserKey()).isNotNull();
        assertThat(savedUser.getUserKey()).isNotBlank();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("hashedpassword");
    }

    @Test
    void save_MultipleUsers_CreatesUniqueKeys() {
        // Given
        User user1 = User.builder()
                .username("user1")
                .email("user1@example.com")
                .password("password1")
                .build();

        User user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .password("password2")
                .build();

        // When
        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);

        // Then
        assertThat(savedUser1.getUserKey()).isNotEqualTo(savedUser2.getUserKey());
        assertThat(savedUser1.getUserKey()).isNotNull();
        assertThat(savedUser2.getUserKey()).isNotNull();
    }

    @Test
    void deleteByUserKey_ExistingUser_RemovesUser() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        userRepository.deleteByUserKey(savedUser.getUserKey());

        // Then
        User foundUser = userRepository.findByUserKey(savedUser.getUserKey());
        assertThat(foundUser).isNull();
    }

    @Test
    void deleteByUserKey_NonExistingUser_DoesNothing() {
        // When & Then - should not throw exception
        userRepository.deleteByUserKey("non-existent-key");
    }
} 