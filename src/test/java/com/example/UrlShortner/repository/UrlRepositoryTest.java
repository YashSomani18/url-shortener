package com.example.UrlShortner.repository;

import com.example.UrlShortner.model.Url;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UrlRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UrlRepository urlRepository;

    private Url testUrl;

    @BeforeEach
    void setUp() {
        testUrl = Url.builder()
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .title("Test URL")
                .description("A test URL for shortening")
                .isActive(true)
                .clickCount(0L)
                .build();
    }

    @Test
    void findByShortCode_ExistingUrl_ReturnsUrl() {
        // Given
        Url savedUrl = entityManager.persistAndFlush(testUrl);

        // When
        Url foundUrl = urlRepository.findByShortCode("abc123");

        // Then
        assertThat(foundUrl).isNotNull();
        assertThat(foundUrl.getShortCode()).isEqualTo("abc123");
        assertThat(foundUrl.getOriginalUrl()).isEqualTo("https://example.com/very-long-url");
        assertThat(foundUrl.getUrlKey()).isEqualTo(savedUrl.getUrlKey());
        assertThat(foundUrl.getIsActive()).isTrue();
    }

    @Test
    void findByShortCode_NonExistingUrl_ReturnsNull() {
        // When
        Url foundUrl = urlRepository.findByShortCode("nonexistent");

        // Then
        assertThat(foundUrl).isNull();
    }

    @Test
    void findByUserKey_ExistingUrls_ReturnsUrls() {
        // Given
        String userKey = "user-key-123";
        Url url1 = Url.builder()
                .originalUrl("https://example.com/1")
                .shortCode("abc123")
                .userKey(userKey)
                .isActive(true)
                .clickCount(0L)
                .build();

        Url url2 = Url.builder()
                .originalUrl("https://example.com/2")
                .shortCode("def456")
                .userKey(userKey)
                .isActive(false)
                .clickCount(5L)
                .build();

        entityManager.persistAndFlush(url1);
        entityManager.persistAndFlush(url2);

        // When
        List<Url> foundUrls = urlRepository.findByUserKey(userKey);

        // Then
        assertThat(foundUrls).hasSize(2);
        assertThat(foundUrls).extracting("shortCode").containsExactlyInAnyOrder("abc123", "def456");
        assertThat(foundUrls).extracting("originalUrl").containsExactlyInAnyOrder("https://example.com/1", "https://example.com/2");
    }

    @Test
    void findByUserKey_NoUrls_ReturnsEmptyList() {
        // When
        List<Url> foundUrls = urlRepository.findByUserKey("nonexistent-user");

        // Then
        assertThat(foundUrls).isEmpty();
    }

    @Test
    void existsByShortCode_ExistingUrl_ReturnsTrue() {
        // Given
        entityManager.persistAndFlush(testUrl);

        // When
        boolean exists = urlRepository.existsByShortCode("abc123");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByShortCode_NonExistingUrl_ReturnsFalse() {
        // When
        boolean exists = urlRepository.existsByShortCode("nonexistent");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void save_NewUrl_CreatesUrlWithGeneratedKey() {
        // When
        Url savedUrl = urlRepository.save(testUrl);

        // Then
        assertThat(savedUrl.getUrlKey()).isNotNull();
        assertThat(savedUrl.getUrlKey()).isNotBlank();
        assertThat(savedUrl.getShortCode()).isEqualTo("abc123");
        assertThat(savedUrl.getOriginalUrl()).isEqualTo("https://example.com/very-long-url");
        assertThat(savedUrl.getIsActive()).isTrue();
        assertThat(savedUrl.getClickCount()).isEqualTo(0L);
    }

    @Test
    void save_MultipleUrls_CreatesUniqueKeys() {
        // Given
        Url url1 = Url.builder()
                .originalUrl("https://example.com/1")
                .shortCode("abc123")
                .isActive(true)
                .clickCount(0L)
                .build();

        Url url2 = Url.builder()
                .originalUrl("https://example.com/2")
                .shortCode("def456")
                .isActive(true)
                .clickCount(0L)
                .build();

        // When
        Url savedUrl1 = urlRepository.save(url1);
        Url savedUrl2 = urlRepository.save(url2);

        // Then
        assertThat(savedUrl1.getUrlKey()).isNotEqualTo(savedUrl2.getUrlKey());
        assertThat(savedUrl1.getUrlKey()).isNotNull();
        assertThat(savedUrl2.getUrlKey()).isNotNull();
    }

    @Test
    void save_UrlWithExpiration_SavesExpirationDate() {
        // Given
        LocalDateTime expiration = LocalDateTime.now().plusDays(30);
        Url urlWithExpiration = Url.builder()
                .originalUrl("https://example.com/expiring")
                .shortCode("exp123")
                .expiresAt(expiration)
                .isActive(true)
                .clickCount(0L)
                .build();

        // When
        Url savedUrl = urlRepository.save(urlWithExpiration);

        // Then
        assertThat(savedUrl.getExpiresAt()).isEqualTo(expiration);
        assertThat(savedUrl.getUrlKey()).isNotNull();
    }

    @Test
    void findByShortCodeAndIsActiveTrue_ActiveUrl_ReturnsUrl() {
        // Given
        Url activeUrl = Url.builder()
                .originalUrl("https://example.com/active")
                .shortCode("active123")
                .isActive(true)
                .clickCount(0L)
                .build();

        entityManager.persistAndFlush(activeUrl);

        // When
        Url foundUrl = urlRepository.findByShortCodeAndIsActiveTrue("active123");

        // Then
        assertThat(foundUrl).isNotNull();
        assertThat(foundUrl.getShortCode()).isEqualTo("active123");
        assertThat(foundUrl.getIsActive()).isTrue();
    }

    @Test
    void findByShortCodeAndIsActiveTrue_InactiveUrl_ReturnsNull() {
        // Given
        Url inactiveUrl = Url.builder()
                .originalUrl("https://example.com/inactive")
                .shortCode("inactive123")
                .isActive(false)
                .clickCount(0L)
                .build();

        entityManager.persistAndFlush(inactiveUrl);

        // When
        Url foundUrl = urlRepository.findByShortCodeAndIsActiveTrue("inactive123");

        // Then
        assertThat(foundUrl).isNull();
    }

    @Test
    void findByShortCodeAndIsActiveTrue_NonExistingUrl_ReturnsNull() {
        // When
        Url foundUrl = urlRepository.findByShortCodeAndIsActiveTrue("nonexistent");

        // Then
        assertThat(foundUrl).isNull();
    }
} 