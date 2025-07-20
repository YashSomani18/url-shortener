package com.example.UrlShortner.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UrlTest {

    @Test
    void builder_CreatesUrlWithAllFields() {
        // Given & When
        LocalDateTime expiration = LocalDateTime.now().plusDays(30);
        Url url = Url.builder()
                .urlKey("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .expiresAt(expiration)
                .clickCount(5L)
                .userKey("user-key-123")
                .isActive(true)
                .title("Test URL")
                .description("A test URL for shortening")
                .build();

        // Then
        assertThat(url.getUrlKey()).isEqualTo("url-key-123");
        assertThat(url.getOriginalUrl()).isEqualTo("https://example.com/very-long-url");
        assertThat(url.getShortCode()).isEqualTo("abc123");
        assertThat(url.getExpiresAt()).isEqualTo(expiration);
        assertThat(url.getClickCount()).isEqualTo(5L);
        assertThat(url.getUserKey()).isEqualTo("user-key-123");
        assertThat(url.getIsActive()).isTrue();
        assertThat(url.getTitle()).isEqualTo("Test URL");
        assertThat(url.getDescription()).isEqualTo("A test URL for shortening");
    }

    @Test
    void builder_CreatesUrlWithDefaultValues() {
        // Given & When
        Url url = Url.builder()
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .build();

        // Then
        assertThat(url.getOriginalUrl()).isEqualTo("https://example.com/very-long-url");
        assertThat(url.getShortCode()).isEqualTo("abc123");
        assertThat(url.getClickCount()).isEqualTo(0L); // Default value
        assertThat(url.getIsActive()).isTrue(); // Default value
        assertThat(url.getUrlKey()).isNull();
        assertThat(url.getUserKey()).isNull();
        assertThat(url.getExpiresAt()).isNull();
        assertThat(url.getTitle()).isNull();
        assertThat(url.getDescription()).isNull();
    }

    @Test
    void noArgsConstructor_CreatesEmptyUrl() {
        // Given & When
        Url url = new Url();

        // Then
        assertThat(url.getUrlKey()).isNull();
        assertThat(url.getOriginalUrl()).isNull();
        assertThat(url.getShortCode()).isNull();
        assertThat(url.getExpiresAt()).isNull();
        assertThat(url.getClickCount()).isEqualTo(0L); // Default value from @Builder.Default
        assertThat(url.getUserKey()).isNull();
        assertThat(url.getIsActive()).isTrue(); // Default value from @Builder.Default
        assertThat(url.getTitle()).isNull();
        assertThat(url.getDescription()).isNull();
    }

    @Test
    void allArgsConstructor_CreatesUrlWithAllFields() {
        // Given & When
        LocalDateTime expiration = LocalDateTime.now().plusDays(30);
        Url url = new Url("url-key-123", "https://example.com/very-long-url", "abc123", 
                         expiration, 5L, "user-key-123", true, "Test URL", "A test URL for shortening");

        // Then
        assertThat(url.getUrlKey()).isEqualTo("url-key-123");
        assertThat(url.getOriginalUrl()).isEqualTo("https://example.com/very-long-url");
        assertThat(url.getShortCode()).isEqualTo("abc123");
        assertThat(url.getExpiresAt()).isEqualTo(expiration);
        assertThat(url.getClickCount()).isEqualTo(5L);
        assertThat(url.getUserKey()).isEqualTo("user-key-123");
        assertThat(url.getIsActive()).isTrue();
        assertThat(url.getTitle()).isEqualTo("Test URL");
        assertThat(url.getDescription()).isEqualTo("A test URL for shortening");
    }

    @Test
    void setters_UpdateUrlFields() {
        // Given
        Url url = new Url();
        LocalDateTime expiration = LocalDateTime.now().plusDays(30);

        // When
        url.setUrlKey("url-key-123");
        url.setOriginalUrl("https://example.com/very-long-url");
        url.setShortCode("abc123");
        url.setExpiresAt(expiration);
        url.setClickCount(10L);
        url.setUserKey("user-key-123");
        url.setIsActive(false);
        url.setTitle("Updated Title");
        url.setDescription("Updated Description");

        // Then
        assertThat(url.getUrlKey()).isEqualTo("url-key-123");
        assertThat(url.getOriginalUrl()).isEqualTo("https://example.com/very-long-url");
        assertThat(url.getShortCode()).isEqualTo("abc123");
        assertThat(url.getExpiresAt()).isEqualTo(expiration);
        assertThat(url.getClickCount()).isEqualTo(10L);
        assertThat(url.getUserKey()).isEqualTo("user-key-123");
        assertThat(url.getIsActive()).isFalse();
        assertThat(url.getTitle()).isEqualTo("Updated Title");
        assertThat(url.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void equals_SameAllFields_ReturnsTrue() {
        // Given
        Url url1 = Url.builder()
                .urlKey("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .title("Test URL")
                .description("A test URL")
                .userKey("user-key-123")
                .build();

        Url url2 = Url.builder()
                .urlKey("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .title("Test URL")
                .description("A test URL")
                .userKey("user-key-123")
                .build();

        // When & Then
        assertThat(url1).isEqualTo(url2);
    }

    @Test
    void equals_DifferentUrlKey_ReturnsFalse() {
        // Given
        Url url1 = Url.builder()
                .urlKey("url-key-1")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .title("Test URL")
                .description("A test URL")
                .userKey("user-key-123")
                .build();

        Url url2 = Url.builder()
                .urlKey("url-key-2")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .title("Test URL")
                .description("A test URL")
                .userKey("user-key-123")
                .build();

        // When & Then
        assertThat(url1).isNotEqualTo(url2);
    }

    @Test
    void equals_NullUrl_ReturnsFalse() {
        // Given
        Url url = Url.builder()
                .urlKey("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .build();

        // When & Then
        assertThat(url).isNotEqualTo(null);
    }

    @Test
    void equals_SameObject_ReturnsTrue() {
        // Given
        Url url = Url.builder()
                .urlKey("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .build();

        // When & Then
        assertThat(url).isEqualTo(url);
    }

    @Test
    void hashCode_SameAllFields_ReturnsSameHashCode() {
        // Given
        Url url1 = Url.builder()
                .urlKey("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .title("Test URL")
                .description("A test URL")
                .userKey("user-key-123")
                .build();

        Url url2 = Url.builder()
                .urlKey("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .title("Test URL")
                .description("A test URL")
                .userKey("user-key-123")
                .build();

        // When & Then
        assertThat(url1.hashCode()).isEqualTo(url2.hashCode());
    }

    @Test
    void hashCode_DifferentUrlKey_ReturnsDifferentHashCode() {
        // Given
        Url url1 = Url.builder()
                .urlKey("url-key-1")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .title("Test URL")
                .description("A test URL")
                .userKey("user-key-123")
                .build();

        Url url2 = Url.builder()
                .urlKey("url-key-2")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .title("Test URL")
                .description("A test URL")
                .userKey("user-key-123")
                .build();

        // When & Then
        assertThat(url1.hashCode()).isNotEqualTo(url2.hashCode());
    }

    @Test
    void toString_ContainsUrlInformation() {
        // Given
        Url url = Url.builder()
                .urlKey("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .title("Test URL")
                .build();

        // When
        String toString = url.toString();

        // Then
        assertThat(toString).contains("url-key-123");
        assertThat(toString).contains("https://example.com/very-long-url");
        assertThat(toString).contains("abc123");
        assertThat(toString).contains("Test URL");
    }

    @Test
    void inheritance_ExtendsBaseAuditableEntity() {
        // Given
        Url url = Url.builder()
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .build();

        // When & Then
        assertThat(url).isInstanceOf(BaseAuditableEntity.class);
    }

    @Test
    void builder_WithExpiredUrl() {
        // Given & When
        LocalDateTime pastExpiration = LocalDateTime.now().minusDays(1);
        Url url = Url.builder()
                .originalUrl("https://example.com/expired")
                .shortCode("exp123")
                .expiresAt(pastExpiration)
                .isActive(true)
                .build();

        // Then
        assertThat(url.getExpiresAt()).isEqualTo(pastExpiration);
        assertThat(url.getIsActive()).isTrue();
    }

    @Test
    void builder_WithAnonymousUrl() {
        // Given & When
        Url url = Url.builder()
                .originalUrl("https://example.com/anonymous")
                .shortCode("anon123")
                .build();

        // Then
        assertThat(url.getUserKey()).isNull();
        assertThat(url.getOriginalUrl()).isEqualTo("https://example.com/anonymous");
        assertThat(url.getShortCode()).isEqualTo("anon123");
    }
} 