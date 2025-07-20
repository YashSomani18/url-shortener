package com.example.UrlShortner.service;

import com.example.UrlShortner.dto.CreateUrlRequest;
import com.example.UrlShortner.dto.UrlResponse;
import com.example.UrlShortner.model.Url;
import com.example.UrlShortner.model.User;
import com.example.UrlShortner.repository.UrlRepository;
import com.example.UrlShortner.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {
    
    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;
    private final AnalyticsService analyticsService;
    private static final Random random = new Random();

    @Value("${host.link}")
    private String hostLink;

    @Value("${CHARACTERS}")
    private String Characters;

    @Value("${SHORT_CODE_LENGTH}")
    private Integer SHORT_CODE_LENGTH;
    
    @Transactional
    public UrlResponse createShortUrl(CreateUrlRequest request, String username) {

        User user = userRepository.findByUsername(username);
        if (Objects.isNull(user)) {
            throw new IllegalArgumentException("User not found: " + username);
        }

        Url existingUrl = urlRepository.findByOriginalUrlAndUserKey(request.getOriginalUrl(), user.getUserKey());
        if (Objects.nonNull(existingUrl)) {
            log.info("URL already exists for user: {}", username);
            return convertToUrlResponse(existingUrl);
        }

        String shortCode = generateUniqueShortCode();

        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .userKey(user.getUserKey())
                .title(request.getTitle())
                .description(request.getDescription())
                .expiresAt(request.getExpiresAt())
                .isActive(true)
                .clickCount(0L)
                .build();

        Url savedUrl = urlRepository.save(url);

        cacheService.cacheUrl(shortCode, savedUrl);
        
        log.info("Created short URL: {} -> {} for user: {}", shortCode, request.getOriginalUrl(), username);
        return convertToUrlResponse(savedUrl);
    }
    
    @Cacheable(value = "urls", key = "#shortCode")
    public UrlResponse getUrlByShortCode(String shortCode) {
        return Optional.ofNullable(urlRepository.findByShortCodeAndIsActiveTrue(shortCode))
                .map(this::convertToUrlResponse)
                .orElse(null);
    }
    
    @Transactional
    public UrlResponse redirectToOriginalUrl(String shortCode, String ipAddress, String userAgent, String referer) {
        UrlResponse response = getUrlByShortCode(shortCode);
        
        if (Objects.nonNull(response)) {
            Url url = urlRepository.findByShortCode(shortCode);

            if (Objects.nonNull(url.getExpiresAt()) && url.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.warn("Attempted to access expired URL: {}", shortCode);
                response.setIsActive(false);
                return null;
            }

            url.setClickCount(url.getClickCount() + 1);
            urlRepository.save(url);

            log.info("UrlKey{}, ipAddress{}, userAgent{}, referer{}",url.getUrlKey(), ipAddress, userAgent, referer);
            analyticsService.recordClick(url.getUrlKey(), ipAddress, userAgent, referer);

            cacheService.cacheUrl(shortCode, url);
            
            log.info("Redirected short URL: {} -> {}", shortCode, url.getOriginalUrl());
        }
        
        return response;
    }
    
    public List<UrlResponse> getUserUrls(String userKey) {
        List<Url> urls = urlRepository.findByUserKeyAndIsActiveTrueOrderByCreatedOnDesc(userKey);
        return urls.stream().map(this::convertToUrlResponse).toList();
    }
    
    @Transactional
    public void deactivateUrl(String urlKey, String userKey) {
        Optional<Url> urlOpt = urlRepository.findById(urlKey);
        if (urlOpt.isPresent() && urlOpt.get().getUserKey().equals(userKey)) {
            Url url = urlOpt.get();
            url.setIsActive(false);
            urlRepository.save(url);
            
            // Remove from cache
            cacheService.removeUrlFromCache(url.getShortCode());
            
            log.info("Deactivated URL: {}", url.getShortCode());
        }
    }
    
    @Transactional
    public void deleteExpiredUrls() {
        List<Url> expiredUrls = urlRepository.findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime.now());
        for (Url url : expiredUrls) {
            url.setIsActive(false);
            urlRepository.save(url);
            cacheService.removeUrlFromCache(url.getShortCode());
        }
        log.info("Deactivated {} expired URLs", expiredUrls.size());
    }
    
    private String generateUniqueShortCode() {
        String shortCode;
        do {
            shortCode = generateRandomString(SHORT_CODE_LENGTH);
        } while (urlRepository.existsByShortCode(shortCode));
        return shortCode;
    }
    
    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(Characters.charAt(random.nextInt(Characters.length())));
        }
        return sb.toString();
    }
    
    @CacheEvict(value = "urls", key = "#url.shortCode")
    public void updateUrl(Url url) {
        urlRepository.save(url);
        cacheService.cacheUrl(url.getShortCode(), url);
    }
    
    public Long getUserUrlCount(String userKey) {
        return urlRepository.countByUserKey(userKey);
    }

    private UrlResponse convertToUrlResponse(Url url) {
        User user = userRepository.findByUserKey(url.getUserKey());
        return getUrlResponse(url, user);
    }

    public UrlResponse getUrlResponse(Url url, User user) {
        return UrlResponse.builder()
                .id(url.getUrlKey())
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .shortUrl(hostLink + url.getShortCode())
                .title(url.getTitle())
                .description(url.getDescription())
                .createdAt(url.getCreatedOn())
                .expiresAt(url.getExpiresAt())
                .clickCount(url.getClickCount())
                .isActive(url.getIsActive())
                .username(user.getUsername())
                .build();
    }


    public String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
} 