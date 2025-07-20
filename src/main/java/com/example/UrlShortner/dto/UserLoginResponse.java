package com.example.UrlShortner.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserLoginResponse {
    String userKey;
    String username;
    String email;
    String token;
} 