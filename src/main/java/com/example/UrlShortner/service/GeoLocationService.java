package com.example.UrlShortner.service;

import com.example.UrlShortner.dto.GeoLocationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GeoLocationService {

    // TODO: Integrate with IP geolocation service (e.g., MaxMind, IPStack, etc.)
    @Cacheable(value = "geoLocation", key = "#ipAddress")
    public GeoLocationDto getLocationInfo(String ipAddress) {
        // Placeholder implementation
        // In production, you'd integrate with services like:
        // - MaxMind GeoIP2
        // - IPStack API
        // - IP2Location

        if (isPrivateIP(ipAddress)) {
            return GeoLocationDto.builder()
                    .country("Unknown")
                    .city("Unknown")
                    .build();
        }

        // Mock implementation - replace with actual service
        return GeoLocationDto.builder()
                .country("Unknown")
                .city("Unknown")
                .build();
    }

    private boolean isPrivateIP(String ip) {
        return ip.startsWith("192.168.") ||
                ip.startsWith("10.") ||
                ip.startsWith("172.") ||
                ip.equals("127.0.0.1") ||
                ip.equals("localhost");
    }
}

