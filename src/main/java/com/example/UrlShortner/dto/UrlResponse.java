package com.example.UrlShortner.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UrlResponse {
    
    String id;
    String originalUrl;
    String shortCode;
    String shortUrl;
    String title;
    String description;
    LocalDateTime createdAt;
    LocalDateTime expiresAt;
    Long clickCount;
    Boolean isActive;
    String username;
} 