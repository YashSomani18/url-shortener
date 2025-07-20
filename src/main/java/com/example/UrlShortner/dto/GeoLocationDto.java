package com.example.UrlShortner.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GeoLocationDto {
    private String country;
    private String city;
    private String region;
    private Double latitude;
    private Double longitude;
}