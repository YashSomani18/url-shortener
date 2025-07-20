package com.example.UrlShortner.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UrlResponseTest {

    @Test
    void builder_CreatesUrlResponseWithAllFields() {
        // Given & When
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        
        UrlResponse response = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .shortUrl("http://localhost:8080/abc123")
                .title("Test URL")
                .description("A test URL for shortening")
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .clickCount(5L)
                .isActive(true)
                .username("testuser")
                .build();

        // Then
        assertThat(response.getId()).isEqualTo("url-key-123");
        assertThat(response.getOriginalUrl()).isEqualTo("https://example.com/very-long-url");
        assertThat(response.getShortCode()).isEqualTo("abc123");
        assertThat(response.getShortUrl()).isEqualTo("http://localhost:8080/abc123");
        assertThat(response.getTitle()).isEqualTo("Test URL");
        assertThat(response.getDescription()).isEqualTo("A test URL for shortening");
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(response.getClickCount()).isEqualTo(5L);
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    void builder_CreatesUrlResponseWithPartialFields() {
        // Given & When
        UrlResponse response = UrlResponse.builder()
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .isActive(true)
                .build();

        // Then
        assertThat(response.getOriginalUrl()).isEqualTo("https://example.com/very-long-url");
        assertThat(response.getShortCode()).isEqualTo("abc123");
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getId()).isNull();
        assertThat(response.getShortUrl()).isNull();
        assertThat(response.getTitle()).isNull();
        assertThat(response.getDescription()).isNull();
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getExpiresAt()).isNull();
        assertThat(response.getClickCount()).isNull();
        assertThat(response.getUsername()).isNull();
    }

    @Test
    void noArgsConstructor_CreatesEmptyUrlResponse() {
        // Given & When
        UrlResponse response = new UrlResponse();

        // Then
        assertThat(response.getId()).isNull();
        assertThat(response.getOriginalUrl()).isNull();
        assertThat(response.getShortCode()).isNull();
        assertThat(response.getShortUrl()).isNull();
        assertThat(response.getTitle()).isNull();
        assertThat(response.getDescription()).isNull();
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getExpiresAt()).isNull();
        assertThat(response.getClickCount()).isNull();
        assertThat(response.getIsActive()).isNull();
        assertThat(response.getUsername()).isNull();
    }

    @Test
    void allArgsConstructor_CreatesUrlResponseWithAllFields() {
        // Given & When
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        
        UrlResponse response = new UrlResponse("url-key-123", "https://example.com/very-long-url", 
                "abc123", "http://localhost:8080/abc123", "Test URL", "A test URL for shortening",
                createdAt, expiresAt, 5L, true, "testuser");

        // Then
        assertThat(response.getId()).isEqualTo("url-key-123");
        assertThat(response.getOriginalUrl()).isEqualTo("https://example.com/very-long-url");
        assertThat(response.getShortCode()).isEqualTo("abc123");
        assertThat(response.getShortUrl()).isEqualTo("http://localhost:8080/abc123");
        assertThat(response.getTitle()).isEqualTo("Test URL");
        assertThat(response.getDescription()).isEqualTo("A test URL for shortening");
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(response.getClickCount()).isEqualTo(5L);
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    void setters_UpdateUrlResponseFields() {
        // Given
        UrlResponse response = new UrlResponse();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);

        // When
        response.setId("url-key-123");
        response.setOriginalUrl("https://example.com/very-long-url");
        response.setShortCode("abc123");
        response.setShortUrl("http://localhost:8080/abc123");
        response.setTitle("Updated Title");
        response.setDescription("Updated Description");
        response.setCreatedAt(createdAt);
        response.setExpiresAt(expiresAt);
        response.setClickCount(10L);
        response.setIsActive(false);
        response.setUsername("updateduser");

        // Then
        assertThat(response.getId()).isEqualTo("url-key-123");
        assertThat(response.getOriginalUrl()).isEqualTo("https://example.com/very-long-url");
        assertThat(response.getShortCode()).isEqualTo("abc123");
        assertThat(response.getShortUrl()).isEqualTo("http://localhost:8080/abc123");
        assertThat(response.getTitle()).isEqualTo("Updated Title");
        assertThat(response.getDescription()).isEqualTo("Updated Description");
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(response.getClickCount()).isEqualTo(10L);
        assertThat(response.getIsActive()).isFalse();
        assertThat(response.getUsername()).isEqualTo("updateduser");
    }

    @Test
    void equals_SameAllFields_ReturnsTrue() {
        // Given
        UrlResponse response1 = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .shortUrl("http://localhost:8080/abc123")
                .title("Test URL")
                .description("A test URL")
                .clickCount(5L)
                .isActive(true)
                .username("testuser")
                .build();

        UrlResponse response2 = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .shortUrl("http://localhost:8080/abc123")
                .title("Test URL")
                .description("A test URL")
                .clickCount(5L)
                .isActive(true)
                .username("testuser")
                .build();

        // When & Then
        assertThat(response1).isEqualTo(response2);
    }

    @Test
    void equals_DifferentId_ReturnsFalse() {
        // Given
        UrlResponse response1 = UrlResponse.builder()
                .id("url-key-1")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .shortUrl("http://localhost:8080/abc123")
                .title("Test URL")
                .description("A test URL")
                .clickCount(5L)
                .isActive(true)
                .username("testuser")
                .build();

        UrlResponse response2 = UrlResponse.builder()
                .id("url-key-2")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .shortUrl("http://localhost:8080/abc123")
                .title("Test URL")
                .description("A test URL")
                .clickCount(5L)
                .isActive(true)
                .username("testuser")
                .build();

        // When & Then
        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    void equals_NullResponse_ReturnsFalse() {
        // Given
        UrlResponse response = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .build();

        // When & Then
        assertThat(response).isNotEqualTo(null);
    }

    @Test
    void equals_SameObject_ReturnsTrue() {
        // Given
        UrlResponse response = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .build();

        // When & Then
        assertThat(response).isEqualTo(response);
    }

    @Test
    void hashCode_SameAllFields_ReturnsSameHashCode() {
        // Given
        UrlResponse response1 = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .shortUrl("http://localhost:8080/abc123")
                .title("Test URL")
                .description("A test URL")
                .clickCount(5L)
                .isActive(true)
                .username("testuser")
                .build();

        UrlResponse response2 = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .shortUrl("http://localhost:8080/abc123")
                .title("Test URL")
                .description("A test URL")
                .clickCount(5L)
                .isActive(true)
                .username("testuser")
                .build();

        // When & Then
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    void hashCode_DifferentId_ReturnsDifferentHashCode() {
        // Given
        UrlResponse response1 = UrlResponse.builder()
                .id("url-key-1")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .shortUrl("http://localhost:8080/abc123")
                .title("Test URL")
                .description("A test URL")
                .clickCount(5L)
                .isActive(true)
                .username("testuser")
                .build();

        UrlResponse response2 = UrlResponse.builder()
                .id("url-key-2")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .shortUrl("http://localhost:8080/abc123")
                .title("Test URL")
                .description("A test URL")
                .clickCount(5L)
                .isActive(true)
                .username("testuser")
                .build();

        // When & Then
        assertThat(response1.hashCode()).isNotEqualTo(response2.hashCode());
    }

    @Test
    void toString_ContainsUrlResponseInformation() {
        // Given
        UrlResponse response = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .title("Test URL")
                .username("testuser")
                .build();

        // When
        String toString = response.toString();

        // Then
        assertThat(toString).contains("url-key-123");
        assertThat(toString).contains("https://example.com/very-long-url");
        assertThat(toString).contains("abc123");
        assertThat(toString).contains("Test URL");
        assertThat(toString).contains("testuser");
    }

    @Test
    void builder_WithExpiredUrl() {
        // Given & When
        LocalDateTime pastExpiration = LocalDateTime.now().minusDays(1);
        UrlResponse response = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl("https://example.com/expired")
                .shortCode("exp123")
                .expiresAt(pastExpiration)
                .isActive(false)
                .build();

        // Then
        assertThat(response.getExpiresAt()).isEqualTo(pastExpiration);
        assertThat(response.getIsActive()).isFalse();
    }

    @Test
    void builder_WithAnonymousUrl() {
        // Given & When
        UrlResponse response = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl("https://example.com/anonymous")
                .shortCode("anon123")
                .isActive(true)
                .build();

        // Then
        assertThat(response.getUsername()).isNull();
        assertThat(response.getOriginalUrl()).isEqualTo("https://example.com/anonymous");
        assertThat(response.getShortCode()).isEqualTo("anon123");
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    void builder_WithHighClickCount() {
        // Given & When
        UrlResponse response = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl("https://example.com/popular")
                .shortCode("pop123")
                .clickCount(1000L)
                .isActive(true)
                .build();

        // Then
        assertThat(response.getClickCount()).isEqualTo(1000L);
        assertThat(response.getIsActive()).isTrue();
    }
} 