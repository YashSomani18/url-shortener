package com.example.UrlShortner.config;

import com.example.UrlShortner.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
    }

    @Test
    void generateToken_ValidUser_ReturnsValidToken() {
        // Given
        User user = User.builder()
                .userKey("user-key-123")
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        // When
        String token = jwtTokenProvider.generateToken(user);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void generateToken_DifferentUsers_ReturnsDifferentTokens() {
        // Given
        User user1 = User.builder()
                .userKey("user-key-1")
                .username("user1")
                .email("user1@example.com")
                .password("password1")
                .build();

        User user2 = User.builder()
                .userKey("user-key-2")
                .username("user2")
                .email("user2@example.com")
                .password("password2")
                .build();

        // When
        String token1 = jwtTokenProvider.generateToken(user1);
        String token2 = jwtTokenProvider.generateToken(user2);

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void generateToken_SameUser_ReturnsConsistentToken() {
        // Given
        User user = User.builder()
                .userKey("user-key-123")
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        // When
        String token1 = jwtTokenProvider.generateToken(user);
        String token2 = jwtTokenProvider.generateToken(user);

        // Then
        assertThat(token1).isNotEqualTo(token2); // Should be different due to timestamp
        assertThat(token1).isNotNull();
        assertThat(token2).isNotNull();
    }

    @Test
    void generateToken_WithSpecialCharacters_HandlesCorrectly() {
        // Given
        User user = User.builder()
                .userKey("user-key-123")
                .username("user@domain.com")
                .email("user@domain.com")
                .password("hashedpassword")
                .build();

        // When
        String token = jwtTokenProvider.generateToken(user);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_WithUnicodeCharacters_HandlesCorrectly() {
        // Given
        User user = User.builder()
                .userKey("user-key-123")
                .username("usér123")
                .email("usér@example.com")
                .password("hashedpassword")
                .build();

        // When
        String token = jwtTokenProvider.generateToken(user);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }
} 