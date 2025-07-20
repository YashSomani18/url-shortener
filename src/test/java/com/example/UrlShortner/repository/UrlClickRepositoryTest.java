package com.example.UrlShortner.repository;

import com.example.UrlShortner.enums.BrowserType;
import com.example.UrlShortner.enums.DeviceType;
import com.example.UrlShortner.enums.OperatingSystem;
import com.example.UrlShortner.model.UrlClick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UrlClickRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UrlClickRepository urlClickRepository;

    private UrlClick testUrlClick;

    @BeforeEach
    void setUp() {
        testUrlClick = UrlClick.builder()
                .urlKey("url-key-123")
                .clickedAt(LocalDateTime.now())
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .referer("https://google.com")
                .country("US")
                .city("New York")
                .deviceType(DeviceType.DESKTOP)
                .browser(BrowserType.CHROME)
                .operatingSystem(OperatingSystem.WINDOWS)
                .isUniqueVisitor(true)
                .isBot(false)
                .build();
    }

    @Test
    void findByUrlKey_ExistingClicks_ReturnsClicks() {
        // Given
        String urlKey = "url-key-123";
        UrlClick click1 = UrlClick.builder()
                .urlKey(urlKey)
                .clickedAt(LocalDateTime.now())
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .isUniqueVisitor(true)
                .isBot(false)
                .build();

        UrlClick click2 = UrlClick.builder()
                .urlKey(urlKey)
                .clickedAt(LocalDateTime.now().plusMinutes(5))
                .ipAddress("192.168.1.2")
                .userAgent("Mozilla/5.0")
                .isUniqueVisitor(false)
                .isBot(false)
                .build();

        entityManager.persistAndFlush(click1);
        entityManager.persistAndFlush(click2);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        var foundClicks = urlClickRepository.findByUrlKeyOrderByClickedAtDesc(urlKey, pageable);

        // Then
        assertThat(foundClicks.getContent()).hasSize(2);
        assertThat(foundClicks.getContent()).extracting("urlKey").allMatch(key -> key.equals(urlKey));
        assertThat(foundClicks.getContent()).extracting("ipAddress").containsExactlyInAnyOrder("192.168.1.1", "192.168.1.2");
    }

    @Test
    void findByUrlKey_NoClicks_ReturnsEmptyPage() {
        // When
        Pageable pageable = PageRequest.of(0, 10);
        var foundClicks = urlClickRepository.findByUrlKeyOrderByClickedAtDesc("nonexistent-url", pageable);

        // Then
        assertThat(foundClicks.getContent()).isEmpty();
    }

    @Test
    void save_NewClick_CreatesClickWithGeneratedKey() {
        // When
        UrlClick savedClick = urlClickRepository.save(testUrlClick);

        // Then
        assertThat(savedClick.getUrlClickKey()).isNotNull();
        assertThat(savedClick.getUrlClickKey()).isNotBlank();
        assertThat(savedClick.getUrlKey()).isEqualTo("url-key-123");
        assertThat(savedClick.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(savedClick.getIsUniqueVisitor()).isTrue();
        assertThat(savedClick.getIsBot()).isFalse();
    }

    @Test
    void save_MultipleClicks_CreatesUniqueKeys() {
        // Given
        UrlClick click1 = UrlClick.builder()
                .urlKey("url-key-1")
                .clickedAt(LocalDateTime.now())
                .ipAddress("192.168.1.1")
                .isUniqueVisitor(true)
                .isBot(false)
                .build();

        UrlClick click2 = UrlClick.builder()
                .urlKey("url-key-2")
                .clickedAt(LocalDateTime.now().plusMinutes(5))
                .ipAddress("192.168.1.2")
                .isUniqueVisitor(true)
                .isBot(false)
                .build();

        // When
        UrlClick savedClick1 = urlClickRepository.save(click1);
        UrlClick savedClick2 = urlClickRepository.save(click2);

        // Then
        assertThat(savedClick1.getUrlClickKey()).isNotEqualTo(savedClick2.getUrlClickKey());
        assertThat(savedClick1.getUrlClickKey()).isNotNull();
        assertThat(savedClick2.getUrlClickKey()).isNotNull();
    }

    @Test
    void save_ClickWithGeoLocation_SavesGeoData() {
        // Given
        UrlClick clickWithGeo = UrlClick.builder()
                .urlKey("url-key-123")
                .clickedAt(LocalDateTime.now())
                .ipAddress("203.0.113.1")
                .country("US")
                .city("San Francisco")
                .region("California")
                .countryCode("USA")
                .timezone("America/Los_Angeles")
                .geoEnriched(true)
                .build();

        // When
        UrlClick savedClick = urlClickRepository.save(clickWithGeo);

        // Then
        assertThat(savedClick.getCountry()).isEqualTo("US");
        assertThat(savedClick.getCity()).isEqualTo("San Francisco");
        assertThat(savedClick.getRegion()).isEqualTo("California");
        assertThat(savedClick.getCountryCode()).isEqualTo("USA");
        assertThat(savedClick.getTimezone()).isEqualTo("America/Los_Angeles");
        assertThat(savedClick.getGeoEnriched()).isTrue();
    }

    @Test
    void save_ClickWithDeviceInfo_SavesDeviceData() {
        // Given
        UrlClick clickWithDevice = UrlClick.builder()
                .urlKey("url-key-123")
                .clickedAt(LocalDateTime.now())
                .ipAddress("192.168.1.1")
                .deviceType(DeviceType.MOBILE)
                .browser(BrowserType.SAFARI)
                .operatingSystem(OperatingSystem.IOS)
                .browserVersion("15.0")
                .screenResolution("375x667")
                .language("en-US")
                .isMobile(true)
                .deviceEnriched(true)
                .build();

        // When
        UrlClick savedClick = urlClickRepository.save(clickWithDevice);

        // Then
        assertThat(savedClick.getDeviceType()).isEqualTo(DeviceType.MOBILE);
        assertThat(savedClick.getBrowser()).isEqualTo(BrowserType.SAFARI);
        assertThat(savedClick.getOperatingSystem()).isEqualTo(OperatingSystem.IOS);
        assertThat(savedClick.getBrowserVersion()).isEqualTo("15.0");
        assertThat(savedClick.getScreenResolution()).isEqualTo("375x667");
        assertThat(savedClick.getLanguage()).isEqualTo("en-US");
        assertThat(savedClick.getIsMobile()).isTrue();
        assertThat(savedClick.getDeviceEnriched()).isTrue();
    }

    @Test
    void save_ClickWithUTMParameters_SavesUTMData() {
        // Given
        UrlClick clickWithUTM = UrlClick.builder()
                .urlKey("url-key-123")
                .clickedAt(LocalDateTime.now())
                .ipAddress("192.168.1.1")
                .utmSource("google")
                .utmMedium("cpc")
                .utmCampaign("summer_sale")
                .utmTerm("url shortener")
                .utmContent("banner_ad")
                .build();

        // When
        UrlClick savedClick = urlClickRepository.save(clickWithUTM);

        // Then
        assertThat(savedClick.getUtmSource()).isEqualTo("google");
        assertThat(savedClick.getUtmMedium()).isEqualTo("cpc");
        assertThat(savedClick.getUtmCampaign()).isEqualTo("summer_sale");
        assertThat(savedClick.getUtmTerm()).isEqualTo("url shortener");
        assertThat(savedClick.getUtmContent()).isEqualTo("banner_ad");
    }

    @Test
    void save_ClickWithAnalyticsData_SavesAnalyticsData() {
        // Given
        UrlClick clickWithAnalytics = UrlClick.builder()
                .urlKey("url-key-123")
                .clickedAt(LocalDateTime.now())
                .ipAddress("192.168.1.1")
                .sessionId("session-123")
                .responseTimeMs(150)
                .isSuspicious(false)
                .clickSource("direct")
                .previousUrl("https://google.com")
                .connectionType("wifi")
                .build();

        // When
        UrlClick savedClick = urlClickRepository.save(clickWithAnalytics);

        // Then
        assertThat(savedClick.getSessionId()).isEqualTo("session-123");
        assertThat(savedClick.getResponseTimeMs()).isEqualTo(150);
        assertThat(savedClick.getIsSuspicious()).isFalse();
        assertThat(savedClick.getClickSource()).isEqualTo("direct");
        assertThat(savedClick.getPreviousUrl()).isEqualTo("https://google.com");
        assertThat(savedClick.getConnectionType()).isEqualTo("wifi");
    }

    @Test
    void findByUrlKeyOrderByClickedAtDesc_ReturnsOrderedClicks() {
        // Given
        String urlKey = "url-key-123";
        LocalDateTime now = LocalDateTime.now();
        
        UrlClick click1 = UrlClick.builder()
                .urlKey(urlKey)
                .clickedAt(now.minusMinutes(10))
                .ipAddress("192.168.1.1")
                .isUniqueVisitor(true)
                .isBot(false)
                .build();

        UrlClick click2 = UrlClick.builder()
                .urlKey(urlKey)
                .clickedAt(now)
                .ipAddress("192.168.1.2")
                .isUniqueVisitor(true)
                .isBot(false)
                .build();

        UrlClick click3 = UrlClick.builder()
                .urlKey(urlKey)
                .clickedAt(now.minusMinutes(5))
                .ipAddress("192.168.1.3")
                .isUniqueVisitor(true)
                .isBot(false)
                .build();

        entityManager.persistAndFlush(click1);
        entityManager.persistAndFlush(click2);
        entityManager.persistAndFlush(click3);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        var foundClicks = urlClickRepository.findByUrlKeyOrderByClickedAtDesc(urlKey, pageable);

        // Then
        assertThat(foundClicks.getContent()).hasSize(3);
        assertThat(foundClicks.getContent().get(0).getClickedAt()).isEqualTo(now); // Most recent first
        assertThat(foundClicks.getContent().get(1).getClickedAt()).isEqualTo(now.minusMinutes(5));
        assertThat(foundClicks.getContent().get(2).getClickedAt()).isEqualTo(now.minusMinutes(10));
    }
} 