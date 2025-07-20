package com.example.UrlShortner.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateUrlRequest {

    @NonNull
    private String originalUrl;

    private String title;

    private String description;
    
    private LocalDateTime expiresAt;
} 