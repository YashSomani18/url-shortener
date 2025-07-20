package com.example.UrlShortner.service;

import com.example.UrlShortner.dto.ClickAnalyticsDto;
import com.example.UrlShortner.dto.UrlAnalyticsDto;
import com.example.UrlShortner.model.UrlClick;
import com.example.UrlShortner.repository.UrlClickRepository;
import com.example.UrlShortner.service.DeviceDetectionService;
import com.example.UrlShortner.service.GeoLocationService;
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

import java.time.LocalDateTime;
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
        try {
            UrlClick click = buildUrlClick(urlKey, ipAddress, userAgent, referer);
            urlClickRepository.save(click);

            log.debug("Recorded click for URL key: {}, IP: {}, Device: {}",
                    urlKey, ipAddress, click.getDeviceType());
        } catch (Exception e) {
            log.error("Failed to record click for URL key: {}", urlKey, e);
            // Don't throw - we don't want analytics failure to break redirects
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

        return click;
    }

    @Cacheable(value = "clickCounts", key = "#urlKey")
    public Long getClickCount(String urlKey) {
        return urlClickRepository.countByUrlKey(urlKey);
    }

    @Cacheable(value = "recentClickCounts", key = "#urlKey + '_' + #since")
    public Long getClickCountSince(String urlKey, LocalDateTime since) {
        return urlClickRepository.countByUrlKeyAndClickedAtGreaterThanEqual(urlKey, since);
    }

    public Page<UrlClick> getClickHistory(String urlKey, Pageable pageable) {
        return urlClickRepository.findByUrlKeyOrderByClickedAtDesc(urlKey, pageable);
    }

    public List<UrlClick> getClickHistory(String urlKey, LocalDateTime startDate, LocalDateTime endDate) {
        return urlClickRepository.findByUrlKeyAndClickedAtBetweenOrderByClickedAtDesc(urlKey, startDate, endDate);
    }

    @Cacheable(value = "analyticsStats", key = "#urlKey + '_country'")
    public Map<String, Long> getClickStatsByCountry(String urlKey) {
        return urlClickRepository.getClickStatsByCountry(urlKey)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Cacheable(value = "analyticsStats", key = "#urlKey + '_browser'")
    public Map<String, Long> getClickStatsByBrowser(String urlKey) {
        return urlClickRepository.getClickStatsByBrowser(urlKey)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Cacheable(value = "analyticsStats", key = "#urlKey + '_device'")
    public Map<String, Long> getClickStatsByDeviceType(String urlKey) {
        return urlClickRepository.getClickStatsByDeviceType(urlKey)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * Get comprehensive analytics for a URL
     */
    @Cacheable(value = "urlAnalytics", key = "#urlKey")
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
}