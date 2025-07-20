package com.example.UrlShortner.model;

import com.example.UrlShortner.enums.BrowserType;
import com.example.UrlShortner.enums.DeviceType;
import com.example.UrlShortner.enums.OperatingSystem;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "url_clicks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
public class UrlClick extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "url_click_key", length = 36)
    String urlClickKey;

    @Column(name = "url_key", nullable = false, length = 36)
    String urlKey;

    @Column(name = "clicked_at", nullable = false, updatable = false)
    LocalDateTime clickedAt;

    @Column(name = "ip_address", length = 45)
    String ipAddress;

    @Column(name = "user_agent", length = 500)
    String userAgent;

    @Column(name = "referer", length = 500)
    String referer;

    @Column(name = "country", length = 100)
    String country;

    @Column(name = "city", length = 100)
    String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 20)
    DeviceType deviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "browser", length = 50)
    BrowserType browser;

    @Enumerated(EnumType.STRING)
    @Column(name = "operating_system", length = 50)
    OperatingSystem operatingSystem;

    @Column(name = "region", length = 100)
    String region;

    @Column(name = "country_code", length = 3)
    String countryCode;

    @Column(name = "timezone", length = 50)
    String timezone;

    @Column(name = "browser_version", length = 20)
    String browserVersion;

    @Column(name = "screen_resolution", length = 20)
    String screenResolution;

    @Column(name = "language", length = 10)
    String language;

    @Column(name = "session_id", length = 36)
    String sessionId;

    @Column(name = "is_unique_visitor")
    Boolean isUniqueVisitor;

    @Column(name = "is_bot")
    Boolean isBot;

    @Column(name = "utm_source", length = 100)
    String utmSource;

    @Column(name = "utm_medium", length = 100)
    String utmMedium;

    @Column(name = "utm_campaign", length = 100)
    String utmCampaign;

    @Column(name = "utm_term", length = 100)
    String utmTerm;

    @Column(name = "utm_content", length = 100)
    String utmContent;

    @Column(name = "response_time_ms")
    Integer responseTimeMs;

    @Column(name = "is_suspicious")
    Boolean isSuspicious;

    @Column(name = "click_source", length = 20)
    String clickSource;

    @Column(name = "previous_url", length = 500)
    String previousUrl;

    @Column(name = "is_mobile")
    Boolean isMobile;

    @Column(name = "connection_type", length = 20)
    String connectionType;

    @Column(name = "geo_enriched")
    Boolean geoEnriched;

    @Column(name = "device_enriched")
    Boolean deviceEnriched;
}
