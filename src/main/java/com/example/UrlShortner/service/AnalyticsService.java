package com.example.UrlShortner.service;

import com.example.UrlShortner.dto.ClickAnalyticsDto;
import com.example.UrlShortner.dto.UrlAnalyticsDto;
import com.example.UrlShortner.model.UrlClick;
import com.example.UrlShortner.repository.UrlClickRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final UrlClickRepository urlClickRepository;
    private final DeviceDetectionService deviceDetectionService;
    private final GeoLocationService geoLocationService;

    /**
     * Asynchronously record a URL click with comprehensive analytics
     * This prevents blocking the redirect response
     */
    @Async("analyticsExecutor")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public CompletableFuture<Void> recordClickAsync(String urlKey, String ipAddress,
                                                    String userAgent, String referer) {
        try {
            recordClick(urlKey, ipAddress, userAgent, referer);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to record click for URL key: {} after retries", urlKey, e);
            throw e;
        }
    }

    @Transactional
    public void recordClick(String urlKey, String ipAddress, String userAgent, String referer) {
        log.info("START: recordClick");
        try {
            UrlClick click = buildUrlClick(urlKey, ipAddress, userAgent, referer);
            urlClickRepository.save(click);

            log.debug("Recorded click for URL key: {}, IP: {}, Device: {}",
                    urlKey, ipAddress, click.getDeviceType());
        } catch (Exception e) {
            log.error("Failed to record click for URL key: {}", urlKey, e);
        }
    }

    private UrlClick buildUrlClick(String urlKey, String ipAddress, String userAgent, String referer) {
        UrlClick click = UrlClick.builder()
                .urlKey(urlKey)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .referer(referer)
                .clickedAt(LocalDateTime.now())
                .build();


        var deviceInfo = deviceDetectionService.extractDeviceInfo(userAgent);
        click.setBrowser(deviceInfo.getBrowser());
        click.setDeviceType(deviceInfo.getDeviceType());
        click.setOperatingSystem(deviceInfo.getOperatingSystem());


        try {
            var geoInfo = geoLocationService.getLocationInfo(ipAddress);
            click.setCountry(geoInfo.getCountry());
            click.setCity(geoInfo.getCity());
        } catch (Exception e) {
            log.warn("Failed to get geo location for IP: {}", ipAddress);
            click.setCountry("Unknown");
            click.setCity("Unknown");
        }
        
        extractUtmParameters(click, referer);

        return click;
    }

    @Cacheable(value = "clickCounts", key = "#urlKey", unless = "#result == null")
    public Long getClickCount(String urlKey) {
        return urlClickRepository.countByUrlKey(urlKey);
    }

    @Cacheable(value = "recentClickCounts", key = "#urlKey + '_' + #since", unless = "#result == null")
    public Long getClickCountSince(String urlKey, LocalDateTime since) {
        return urlClickRepository.countByUrlKeyAndClickedAtGreaterThanEqual(urlKey, since);
    }

    public Page<UrlClick> getClickHistory(String urlKey, Pageable pageable) {
        return urlClickRepository.findByUrlKeyOrderByClickedAtDesc(urlKey, pageable);
    }

    public List<UrlClick> getClickHistory(String urlKey, LocalDateTime startDate, LocalDateTime endDate) {
        return urlClickRepository.findByUrlKeyAndClickedAtBetweenOrderByClickedAtDesc(urlKey, startDate, endDate);
    }

    @Cacheable(value = "analyticsStats", key = "#urlKey + '_country'", unless = "#result == null or #result.isEmpty()")
    public Map<String, Long> getClickStatsByCountry(String urlKey) {
        return urlClickRepository.getClickStatsByCountry(urlKey)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> row[0] != null ? row[0].toString() : "Unknown",
                        row -> (Long) row[1]
                ));
    }

    @Cacheable(value = "analyticsStats", key = "#urlKey + '_browser'", unless = "#result == null or #result.isEmpty()")
    public Map<String, Long> getClickStatsByBrowser(String urlKey) {
        return urlClickRepository.getClickStatsByBrowser(urlKey)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> row[0] != null ? row[0].toString() : "Unknown",
                        row -> (Long) row[1]
                ));
    }

    @Cacheable(value = "analyticsStats", key = "#urlKey + '_device'", unless = "#result == null or #result.isEmpty()")
    public Map<String, Long> getClickStatsByDeviceType(String urlKey) {
        return urlClickRepository.getClickStatsByDeviceType(urlKey)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> row[0] != null ? row[0].toString() : "Unknown",
                        row -> (Long) row[1]
                ));
    }

    @Cacheable(value = "analyticsStats", key = "#urlKey + '_utm_source'", unless = "#result == null or #result.isEmpty()")
    public Map<String, Long> getClickStatsByUtmSource(String urlKey) {
        return urlClickRepository.getClickStatsByUtmSource(urlKey)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Cacheable(value = "analyticsStats", key = "#urlKey + '_utm_medium'", unless = "#result == null or #result.isEmpty()")
    public Map<String, Long> getClickStatsByUtmMedium(String urlKey) {
        return urlClickRepository.getClickStatsByUtmMedium(urlKey)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Cacheable(value = "analyticsStats", key = "#urlKey + '_utm_campaign'", unless = "#result == null or #result.isEmpty()")
    public Map<String, Long> getClickStatsByUtmCampaign(String urlKey) {
        return urlClickRepository.getClickStatsByUtmCampaign(urlKey)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> row[0] != null ? row[0].toString() : "Unknown",
                        row -> (Long) row[1]
                ));
    }

    @Cacheable(value = "analyticsStats", key = "#urlKey + '_operating_system'", unless = "#result == null or #result.isEmpty()")
    public Map<String, Long> getClickStatsByOperatingSystem(String urlKey) {
        return urlClickRepository.getClickStatsByOperatingSystem(urlKey)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> row[0] != null ? row[0].toString() : "Unknown",
                        row -> (Long) row[1]
                ));
    }

    /**
     * Get comprehensive analytics for a URL
     */
    @Cacheable(value = "urlAnalytics", key = "#urlKey", unless = "#result == null")
    public UrlAnalyticsDto getUrlAnalytics(String urlKey) {
        return UrlAnalyticsDto.builder()
                .urlKey(urlKey)
                .totalClicks(getClickCount(urlKey))
                .clicksToday(getClickCountSince(urlKey, LocalDateTime.now().minusDays(1)))
                .clicksThisWeek(getClickCountSince(urlKey, LocalDateTime.now().minusWeeks(1)))
                .clicksThisMonth(getClickCountSince(urlKey, LocalDateTime.now().minusMonths(1)))
                .countryStats(getClickStatsByCountry(urlKey))
                .browserStats(getClickStatsByBrowser(urlKey))
                .deviceStats(getClickStatsByDeviceType(urlKey))
                .utmSourceStats(getClickStatsByUtmSource(urlKey))
                .utmMediumStats(getClickStatsByUtmMedium(urlKey))
                .utmCampaignStats(getClickStatsByUtmCampaign(urlKey))
                .operatingSystemStats(getClickStatsByOperatingSystem(urlKey))
                .build();
    }

    /**
     * Get time-based analytics (hourly clicks for the last 24 hours)
     */
    public List<ClickAnalyticsDto> getHourlyClickStats(String urlKey, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return urlClickRepository.getHourlyClickStats(urlKey, since);
    }

    /**
     * Get daily analytics for a date range
     */
    public List<ClickAnalyticsDto> getDailyClickStats(String urlKey, LocalDateTime startDate, LocalDateTime endDate) {
        return urlClickRepository.getDailyClickStats(urlKey, startDate, endDate);
    }

    /**
     * Extract UTM parameters from referer URL and set them on the UrlClick entity
     */
    private void extractUtmParameters(UrlClick click, String referer) {
        if (referer == null || referer.trim().isEmpty()) {
            return;
        }

        try {
            // Find the query string part of the URL
            int queryIndex = referer.indexOf('?');
            if (queryIndex == -1) {
                return; // No query parameters
            }

            String queryString = referer.substring(queryIndex + 1);
            Map<String, String> params = parseQueryString(queryString);

            // Set UTM parameters if they exist
            click.setUtmSource(params.get("utm_source"));
            click.setUtmMedium(params.get("utm_medium"));
            click.setUtmCampaign(params.get("utm_campaign"));
            click.setUtmTerm(params.get("utm_term"));
            click.setUtmContent(params.get("utm_content"));

            log.debug("Extracted UTM parameters for referer: {} - source: {}, medium: {}, campaign: {}",
                    referer, params.get("utm_source"), params.get("utm_medium"), params.get("utm_campaign"));

        } catch (Exception e) {
            log.warn("Failed to extract UTM parameters from referer: {}", referer, e);
        }
    }

    /**
     * Parse query string into a Map of parameter names and values
     */
    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        
        if (queryString == null || queryString.trim().isEmpty()) {
            return params;
        }

        try {
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                    params.put(key, value);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse query string: {}", queryString, e);
        }

        return params;
    }
}