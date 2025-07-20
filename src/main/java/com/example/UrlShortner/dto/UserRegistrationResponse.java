package com.example.UrlShortner.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRegistrationResponse {
    String userKey;
    String username;
    String email;
    LocalDateTime createdOn;
} 