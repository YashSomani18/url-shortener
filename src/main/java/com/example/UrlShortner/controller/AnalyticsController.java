package com.example.UrlShortner.controller;

import com.example.UrlShortner.dto.ClickAnalyticsDto;
import com.example.UrlShortner.dto.UrlAnalyticsDto;
import com.example.UrlShortner.model.UrlClick;
import com.example.UrlShortner.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/{urlKey}/overview")
    public ResponseEntity<UrlAnalyticsDto> getUrlAnalytics(@PathVariable String urlKey) {
        log.debug("Getting analytics overview for URL key: {}", urlKey);
        UrlAnalyticsDto analytics = analyticsService.getUrlAnalytics(urlKey);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/{urlKey}/clicks/count")
    public ResponseEntity<Long> getTotalClicks(@PathVariable String urlKey) {
        Long count = analyticsService.getClickCount(urlKey);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{urlKey}/clicks/recent")
    public ResponseEntity<Map<String, Long>> getRecentClicks(@PathVariable String urlKey) {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Long> recentStats = Map.of(
                "last24Hours", analyticsService.getClickCountSince(urlKey, now.minusDays(1)),
                "lastWeek", analyticsService.getClickCountSince(urlKey, now.minusWeeks(1)),
                "lastMonth", analyticsService.getClickCountSince(urlKey, now.minusMonths(1))
        );
        return ResponseEntity.ok(recentStats);
    }

    @GetMapping("/{urlKey}/clicks/history")
    public ResponseEntity<Page<UrlClick>> getClickHistory(
            @PathVariable String urlKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<UrlClick> history = analyticsService.getClickHistory(urlKey, pageable);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{urlKey}/clicks/history/range")
    public ResponseEntity<List<UrlClick>> getClickHistoryByDateRange(
            @PathVariable String urlKey,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<UrlClick> history = analyticsService.getClickHistory(urlKey, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{urlKey}/stats/country")
    public ResponseEntity<Map<String, Long>> getClickStatsByCountry(@PathVariable String urlKey) {
        Map<String, Long> stats = analyticsService.getClickStatsByCountry(urlKey);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{urlKey}/stats/browser")
    public ResponseEntity<Map<String, Long>> getClickStatsByBrowser(@PathVariable String urlKey) {
        Map<String, Long> stats = analyticsService.getClickStatsByBrowser(urlKey);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{urlKey}/stats/device")
    public ResponseEntity<Map<String, Long>> getClickStatsByDevice(@PathVariable String urlKey) {
        Map<String, Long> stats = analyticsService.getClickStatsByDeviceType(urlKey);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{urlKey}/trends/hourly")
    public ResponseEntity<List<ClickAnalyticsDto>> getHourlyTrends(
            @PathVariable String urlKey,
            @RequestParam(defaultValue = "24") int hours) {

        List<ClickAnalyticsDto> trends = analyticsService.getHourlyClickStats(urlKey, hours);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/{urlKey}/trends/daily")
    public ResponseEntity<List<ClickAnalyticsDto>> getDailyTrends(
            @PathVariable String urlKey,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<ClickAnalyticsDto> trends = analyticsService.getDailyClickStats(urlKey, startDate, endDate);
        return ResponseEntity.ok(trends);
    }
}