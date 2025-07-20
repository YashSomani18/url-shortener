package com.example.UrlShortner.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void builder_CreatesUserWithAllFields() {
        // Given & When
        User user = User.builder()
                .userKey("user-key-123")
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        // Then
        assertThat(user.getUserKey()).isEqualTo("user-key-123");
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("hashedpassword");
    }

    @Test
    void builder_CreatesUserWithPartialFields() {
        // Given & When
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        // Then
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getUserKey()).isNull();
        assertThat(user.getPassword()).isNull();
    }

    @Test
    void noArgsConstructor_CreatesEmptyUser() {
        // Given & When
        User user = new User();

        // Then
        assertThat(user.getUserKey()).isNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPassword()).isNull();
    }

    @Test
    void allArgsConstructor_CreatesUserWithAllFields() {
        // Given & When
        User user = new User("user-key-123", "testuser", "test@example.com", "hashedpassword");

        // Then
        assertThat(user.getUserKey()).isEqualTo("user-key-123");
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("hashedpassword");
    }

    @Test
    void setters_UpdateUserFields() {
        // Given
        User user = new User();

        // When
        user.setUserKey("user-key-123");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("hashedpassword");

        // Then
        assertThat(user.getUserKey()).isEqualTo("user-key-123");
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("hashedpassword");
    }

    @Test
    void equals_SameAllFields_ReturnsTrue() {
        // Given
        User user1 = User.builder()
                .userKey("user-key-123")
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        User user2 = User.builder()
                .userKey("user-key-123")
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        // When & Then
        assertThat(user1).isEqualTo(user2);
    }

    @Test
    void equals_DifferentUserKey_ReturnsFalse() {
        // Given
        User user1 = User.builder()
                .userKey("user-key-1")
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        User user2 = User.builder()
                .userKey("user-key-2")
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        // When & Then
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    void equals_NullUser_ReturnsFalse() {
        // Given
        User user = User.builder()
                .userKey("user-key-123")
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        // When & Then
        assertThat(user).isNotEqualTo(null);
    }

    @Test
    void equals_SameObject_ReturnsTrue() {
        // Given
        User user = User.builder()
                .userKey("user-key-123")
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        // When & Then
        assertThat(user).isEqualTo(user);
    }

    @Test
    void hashCode_SameAllFields_ReturnsSameHashCode() {
        // Given
        User user1 = User.builder()
                .userKey("user-key-123")
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        User user2 = User.builder()
                .userKey("user-key-123")
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        // When & Then
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void hashCode_DifferentUserKey_ReturnsDifferentHashCode() {
        // Given
        User user1 = User.builder()
                .userKey("user-key-1")
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        User user2 = User.builder()
                .userKey("user-key-2")
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        // When & Then
        assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }

    @Test
    void toString_ContainsUserInformation() {
        // Given
        User user = User.builder()
                .userKey("user-key-123")
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        // When
        String toString = user.toString();

        // Then
        assertThat(toString).contains("user-key-123");
        assertThat(toString).contains("testuser");
        assertThat(toString).contains("test@example.com");
        // Note: Lombok's @Data includes all fields in toString, including password
        // In a real application, you might want to use @ToString(exclude = "password")
        assertThat(toString).contains("hashedpassword");
    }
} 