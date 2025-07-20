package com.example.UrlShortner.controller;

import com.example.UrlShortner.dto.UrlResponse;
import com.example.UrlShortner.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/redirect")
public class RedirectController {
    
    private final UrlService urlService;

    /**
     * Redirects a short code to its original URL, tracks analytics, and handles expired/invalid URLs.
     * @param shortCode The short code to redirect.
     * @param request The HttpServletRequest for client IP extraction.
     * @param userAgent The User-Agent header from the client (optional).
     * @param referer The Referer header from the client (optional).
     * @return 302 redirect to the original URL, 404 if not found, 410 if expired.
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "Referer", required = false) String referer
    ) {
        String clientIp = urlService.getClientIpAddress(request);

        UrlResponse urlResponse = urlService.redirectToOriginalUrl(shortCode, clientIp, userAgent, referer);

        if (Objects.isNull(urlResponse)) {
            return ResponseEntity.notFound().build();
        }
        if (Boolean.FALSE.equals(urlResponse.getIsActive())) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(urlResponse.getOriginalUrl()))
                .build();
    }

} 