package com.example.UrlShortner.dto;

import com.example.UrlShortner.enums.BrowserType;
import com.example.UrlShortner.enums.DeviceType;
import com.example.UrlShortner.enums.OperatingSystem;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeviceInfoDto {
    private BrowserType browser;
    private DeviceType deviceType;
    private OperatingSystem operatingSystem;

    public static DeviceInfoDto unknown() {
        return DeviceInfoDto.builder()
                .browser(BrowserType.UNKNOWN)
                .deviceType(DeviceType.UNKNOWN)
                .operatingSystem(OperatingSystem.UNKNOWN)
                .build();
    }
}

