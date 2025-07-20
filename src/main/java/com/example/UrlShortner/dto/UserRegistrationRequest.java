package com.example.UrlShortner.dto;


import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRegistrationRequest {

    @NonNull
    private String username;

    @Email
    private String email;

    @NonNull
    private String password;
} 