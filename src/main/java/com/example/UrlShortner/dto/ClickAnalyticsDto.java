package com.example.UrlShortner.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClickAnalyticsDto {
    private LocalDateTime timestamp;
    private Long clickCount;
}