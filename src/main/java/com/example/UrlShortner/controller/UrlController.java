package com.example.UrlShortner.controller;

import com.example.UrlShortner.dto.CreateUrlRequest;
import com.example.UrlShortner.dto.UrlResponse;
import com.example.UrlShortner.model.User;
import com.example.UrlShortner.service.UrlService;
import com.example.UrlShortner.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
@Slf4j
public class UrlController {
    
    private final UrlService urlService;
    private final UserService userService;


    /**
     * Creates a new short URL for the given original URL and user.
     * @param request The request body containing the original URL, title, description, and expiration.
     * @param username (Optional) The username of the user creating the short URL.
     * @return The created UrlResponse with short code and details.
     */
    @PostMapping
    public ResponseEntity<UrlResponse> createShortUrl(@Valid @RequestBody CreateUrlRequest request,
                                                    @RequestParam(required = false) String username) {
        try {
            UrlResponse response = urlService.createShortUrl(request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error creating short URL: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error creating short URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Retrieves information about a short URL by its short code.
     * @param shortCode The short code of the URL.
     * @return The UrlResponse with details, or 404 if not found.
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlResponse> getUrlInfo(@PathVariable String shortCode) {
        try {
            UrlResponse response = urlService.getUrlByShortCode(shortCode);
            return Optional.ofNullable(response)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting URL info for: {}", shortCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Retrieves all URLs created by a specific user.
     * @param username The username whose URLs to fetch.
     * @return List of UrlResponse objects for the user.
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<UrlResponse>> getUserUrls(@PathVariable String username) {
        try {
            User user = Optional.ofNullable(userService.getUserByUsername(username))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            List<UrlResponse> urls = urlService.getUserUrls(user.getUserKey());

            return ResponseEntity.ok(urls);
        } catch (IllegalArgumentException e) {
            log.error("Error getting user URLs: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error getting user URLs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Deactivates a short URL for a user.
     * @param urlKey The unique key of the URL to deactivate.
     * @param username The username of the user requesting deactivation.
     * @return 200 OK if deactivated, 400 if error, 500 if server error.
     */
    @DeleteMapping("/{urlKey}")
    public ResponseEntity<Void> deactivateUrl(@PathVariable String urlKey, @RequestParam String username) {
        try {
            User user = Optional.ofNullable(userService.getUserByUsername(username))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            urlService.deactivateUrl(urlKey, user.getUserKey());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deactivating URL: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error deactivating URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
//    @GetMapping("/stats/{shortCode}")
//    public ResponseEntity<Map<String, Object>> getUrlStats(@PathVariable String shortCode) {
//        try {
//            return urlService.getUrlByShortCode(shortCode)
//                    .map(url -> {
//                        // TODO: Add analytics service to get detailed stats
//                        Map<String, Object> stats = Map.of(
//                            "shortCode", url.getShortCode(),
//                            "clickCount", url.getClickCount(),
//                            "createdOn", url.getCreatedOn(),
//                            "isActive", url.getIsActive()
//                        );
//                        return ResponseEntity.ok(stats);
//                    })
//                    .orElse(ResponseEntity.notFound().build());
//        } catch (Exception e) {
//            log.error("Error getting URL stats for: {}", shortCode, e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
    

} 