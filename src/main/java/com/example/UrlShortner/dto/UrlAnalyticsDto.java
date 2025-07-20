package com.example.UrlShortner.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UrlAnalyticsDto {
    private String urlKey;
    private Long totalClicks;
    private Long clicksToday;
    private Long clicksThisWeek;
    private Long clicksThisMonth;
    private Map<String, Long> countryStats;
    private Map<String, Long> browserStats;
    private Map<String, Long> deviceStats;
}
