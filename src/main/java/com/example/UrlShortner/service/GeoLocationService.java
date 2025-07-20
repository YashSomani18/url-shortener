package com.example.UrlShortner.service;

import com.example.UrlShortner.dto.GeoLocationDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeoLocationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${geolocation.api.key:}")
    private String apiKey;

    @Value("${geolocation.api.url:http://api.ipstack.com}")
    private String apiUrl;

    @Value("${geolocation.enabled:true}")
    private boolean geoLocationEnabled;

    private static final String UNKNOWN_COUNTRY = "Unknown";
    private static final String UNKNOWN_CITY = "Unknown";

    @Cacheable(value = "geoLocation", key = "#ipAddress")
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public GeoLocationDto getLocationInfo(String ipAddress) {
        if (!geoLocationEnabled || ipAddress == null || ipAddress.trim().isEmpty()) {
            return createUnknownLocation();
        }

        if (isPrivateOrLocalIP(ipAddress)) {
            log.debug("Private/Local IP detected: {}", ipAddress);
            return GeoLocationDto.builder()
                    .country("Local")
                    .city("Local")
                    .region("Local")
                    .latitude(0.0)
                    .longitude(0.0)
                    .build();
        }

        try {
            return fetchLocationFromAPI(ipAddress);
        } catch (Exception e) {
            log.warn("Failed to get geolocation for IP: {} - {}", ipAddress, e.getMessage());
            return createUnknownLocation();
        }
    }

    private GeoLocationDto fetchLocationFromAPI(String ipAddress) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("Geolocation API key not configured, using free tier");
            return fetchFromFreeService(ipAddress);
        }

        return fetchFromIPStack(ipAddress);
    }

    private GeoLocationDto fetchFromIPStack(String ipAddress) {
        String url = String.format("%s/%s?access_key=%s&format=1", apiUrl, ipAddress, apiKey);

        try {
            String response = restTemplate.getForObject(url, String.class);
            IPStackResponse ipStackResponse = objectMapper.readValue(response, IPStackResponse.class);

            if (ipStackResponse.isSuccess()) {
                return GeoLocationDto.builder()
                        .country(ipStackResponse.getCountryName())
                        .countryCode(ipStackResponse.getCountryCode())
                        .city(ipStackResponse.getCity())
                        .region(ipStackResponse.getRegionName())
                        .latitude(ipStackResponse.getLatitude())
                        .longitude(ipStackResponse.getLongitude())
                        .timezone(ipStackResponse.getTimeZone() != null ? ipStackResponse.getTimeZone().getId() : null)
                        .isp(ipStackResponse.getConnection() != null ? ipStackResponse.getConnection().getIsp() : null)
                        .build();
            } else {
                log.warn("IPStack API error for IP {}: {}", ipAddress, ipStackResponse.getError());
                return createUnknownLocation();
            }
        } catch (Exception e) {
            log.error("Error calling IPStack API for IP: {}", ipAddress, e);
            throw new RuntimeException("Geolocation service unavailable", e);
        }
    }

    private GeoLocationDto fetchFromFreeService(String ipAddress) {
        String url = String.format("http://ip-api.com/json/%s", ipAddress);

        try {
            IPApiResponse response = restTemplate.getForObject(url, IPApiResponse.class);

            if (response != null && "success".equals(response.getStatus())) {
                return GeoLocationDto.builder()
                        .country(response.getCountry())
                        .countryCode(response.getCountryCode())
                        .city(response.getCity())
                        .region(response.getRegionName())
                        .latitude(response.getLat())
                        .longitude(response.getLon())
                        .timezone(response.getTimezone())
                        .isp(response.getIsp())
                        .build();
            } else {
                log.warn("IP-API error for IP {}: {}", ipAddress, response != null ? response.getMessage() : "Unknown error");
                return createUnknownLocation();
            }
        } catch (Exception e) {
            log.error("Error calling IP-API for IP: {}", ipAddress, e);
            return createUnknownLocation();
        }
    }

    private boolean isPrivateOrLocalIP(String ip) {
        if (ip == null) return true;

        return ip.startsWith("192.168.") ||
                ip.startsWith("10.") ||
                (ip.startsWith("172.") && isInRange172(ip)) ||
                ip.equals("127.0.0.1") ||
                ip.equals("localhost") ||
                ip.equals("0:0:0:0:0:0:0:1") ||
                ip.equals("::1") ||
                ip.startsWith("fe80:") ||
                ip.startsWith("fc00:") ||
                ip.startsWith("fd00:");
    }

    private boolean isInRange172(String ip) {
        try {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                int secondOctet = Integer.parseInt(parts[1]);
                return secondOctet >= 16 && secondOctet <= 31;
            }
        } catch (NumberFormatException e) {
            log.debug("Invalid IP format: {}", ip);
        }
        return false;
    }

    private GeoLocationDto createUnknownLocation() {
        return GeoLocationDto.builder()
                .country(UNKNOWN_COUNTRY)
                .city(UNKNOWN_CITY)
                .region(UNKNOWN_COUNTRY)
                .latitude(0.0)
                .longitude(0.0)
                .build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class IPStackResponse {
        private String ip;
        private String countryCode;
        private String countryName;
        private String regionCode;
        private String regionName;
        private String city;
        private String zipCode;
        private Double latitude;
        private Double longitude;
        private TimeZone timeZone;
        private Connection connection;
        private Error error;

        public boolean isSuccess() {
            return error == null;
        }

        // Getters and setters
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }

        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

        public String getCountryName() { return countryName; }
        public void setCountryName(String countryName) { this.countryName = countryName; }

        public String getRegionCode() { return regionCode; }
        public void setRegionCode(String regionCode) { this.regionCode = regionCode; }

        public String getRegionName() { return regionName; }
        public void setRegionName(String regionName) { this.regionName = regionName; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }

        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }

        public TimeZone getTimeZone() { return timeZone; }
        public void setTimeZone(TimeZone timeZone) { this.timeZone = timeZone; }

        public Connection getConnection() { return connection; }
        public void setConnection(Connection connection) { this.connection = connection; }

        public Error getError() { return error; }
        public void setError(Error error) { this.error = error; }

        private static class TimeZone {
            private String id;
            private String code;

            public String getId() { return id; }
            public void setId(String id) { this.id = id; }

            public String getCode() { return code; }
            public void setCode(String code) { this.code = code; }
        }

        private static class Connection {
            private String isp;

            public String getIsp() { return isp; }
            public void setIsp(String isp) { this.isp = isp; }
        }

        private static class Error {
            private int code;
            private String info;

            public int getCode() { return code; }
            public void setCode(int code) { this.code = code; }

            public String getInfo() { return info; }
            public void setInfo(String info) { this.info = info; }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class IPApiResponse {
        private String status;
        private String message;
        private String country;
        private String countryCode;
        private String regionName;
        private String city;
        private Double lat;
        private Double lon;
        private String timezone;
        private String isp;

        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

        public String getRegionName() { return regionName; }
        public void setRegionName(String regionName) { this.regionName = regionName; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }

        public Double getLon() { return lon; }
        public void setLon(Double lon) { this.lon = lon; }

        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }

        public String getIsp() { return isp; }
        public void setIsp(String isp) { this.isp = isp; }
    }
}