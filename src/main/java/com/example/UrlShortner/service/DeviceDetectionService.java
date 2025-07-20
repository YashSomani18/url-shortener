package com.example.UrlShortner.service;

import com.example.UrlShortner.dto.DeviceInfoDto;
import com.example.UrlShortner.enums.BrowserType;
import com.example.UrlShortner.enums.DeviceType;
import com.example.UrlShortner.enums.OperatingSystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeviceDetectionService {

    private static final String EDGE = "edg/";
    private static final String EDGE_LEGACY = "edge/";
    private static final String CHROME = "chrome/";
    private static final String CHROMIUM = "chromium";
    private static final String FIREFOX = "firefox/";
    private static final String GECKO = "gecko/";
    private static final String SAFARI = "safari/";
    private static final String OPERA = "opera";
    private static final String OPERA_NEW = "opr/";
    private static final String TRIDENT = "trident";
    private static final String MSIE = "msie";
    private static final String SAMSUNG_BROWSER = "samsungbrowser";
    private static final String UC_BROWSER = "ucbrowser";
    private static final String UC_BROWSER_SPACE = "uc browser";

    private static final String MOBILE = "mobile";
    private static final String ANDROID = "android";
    private static final String IPHONE = "iphone";
    private static final String IPOD = "ipod";
    private static final String TABLET = "tablet";
    private static final String IPAD = "ipad";
    private static final String SMART_TV = "smart-tv";
    private static final String SMARTTV = "smarttv";
    private static final String TV = "tv";
    private static final String PLAYSTATION = "playstation";
    private static final String XBOX = "xbox";
    private static final String NINTENDO = "nintendo";
    private static final String IOT = "iot";
    private static final String EMBEDDED = "embedded";
    private static final String HEADLESS = "headless";

    private static final String WINDOWS_NT = "windows nt";
    private static final String WINDOWS = "windows";
    private static final String MAC_OS_X = "mac os x";
    private static final String MACOS = "macos";
    private static final String IOS = "ios";
    private static final String UBUNTU = "ubuntu";
    private static final String FEDORA = "fedora";
    private static final String CENTOS = "centos";
    private static final String DEBIAN = "debian";
    private static final String FREEBSD = "freebsd";
    private static final String CROS = "cros";
    private static final String CHROMEOS = "chromeos";
    private static final String LINUX = "linux";

    @Cacheable(value = "deviceInfo", key = "#userAgent")
    public DeviceInfoDto extractDeviceInfo(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return DeviceInfoDto.unknown();
        }

        String lowerUserAgent = userAgent.toLowerCase();

        return DeviceInfoDto.builder()
                .browser(detectBrowser(lowerUserAgent))
                .deviceType(detectDeviceType(lowerUserAgent))
                .operatingSystem(detectOperatingSystem(lowerUserAgent))
                .build();
    }

    private BrowserType detectBrowser(String userAgent) {
        if (userAgent.contains(EDGE) || userAgent.contains(EDGE_LEGACY)) {
            return BrowserType.EDGE;
        }

        if (userAgent.contains(CHROME) && !userAgent.contains(CHROMIUM)) {
            return BrowserType.CHROME;
        }

        if (userAgent.contains(FIREFOX) || userAgent.contains(GECKO)) {
            return BrowserType.FIREFOX;
        }

        if (userAgent.contains(SAFARI) && !userAgent.contains(CHROME)) {
            return BrowserType.SAFARI;
        }

        if (userAgent.contains(OPERA) || userAgent.contains(OPERA_NEW)) {
            return BrowserType.OPERA;
        }

        if (userAgent.contains(TRIDENT) || userAgent.contains(MSIE)) {
            return BrowserType.INTERNET_EXPLORER;
        }

        if (userAgent.contains(SAMSUNG_BROWSER)) {
            return BrowserType.SAMSUNG_BROWSER;
        }

        if (userAgent.contains(UC_BROWSER) || userAgent.contains(UC_BROWSER_SPACE)) {
            return BrowserType.UC_BROWSER;
        }

        return BrowserType.UNKNOWN;
    }

    private DeviceType detectDeviceType(String userAgent) {
        if (userAgent.contains(MOBILE) || userAgent.contains(ANDROID) && userAgent.contains(MOBILE)) {
            return DeviceType.MOBILE;
        }

        if (userAgent.contains(IPHONE) || userAgent.contains(IPOD)) {
            return DeviceType.MOBILE;
        }

        if (userAgent.contains(TABLET) || userAgent.contains(IPAD)) {
            return DeviceType.TABLET;
        }

        if (userAgent.contains(ANDROID) && !userAgent.contains(MOBILE)) {
            return DeviceType.TABLET;
        }

        if (userAgent.contains(SMART_TV) || userAgent.contains(SMARTTV) || userAgent.contains(TV)) {
            return DeviceType.TV;
        }

        if (userAgent.contains(PLAYSTATION) || userAgent.contains(XBOX) || userAgent.contains(NINTENDO)) {
            return DeviceType.GAMING_CONSOLE;
        }

        if (userAgent.contains(IOT) || userAgent.contains(EMBEDDED) || userAgent.contains(HEADLESS)) {
            return DeviceType.IOT_DEVICE;
        }

        return DeviceType.DESKTOP;
    }

    private OperatingSystem detectOperatingSystem(String userAgent) {
        if (userAgent.contains(WINDOWS_NT) || userAgent.contains(WINDOWS)) {
            return OperatingSystem.WINDOWS;
        }

        if (userAgent.contains(MAC_OS_X) || userAgent.contains(MACOS)) {
            return OperatingSystem.MACOS;
        }

        if (userAgent.contains(IOS) || userAgent.contains(IPHONE) || userAgent.contains(IPAD)) {
            return OperatingSystem.IOS;
        }

        if (userAgent.contains(ANDROID)) {
            return OperatingSystem.ANDROID;
        }

        if (userAgent.contains(UBUNTU)) {
            return OperatingSystem.UBUNTU;
        }

        if (userAgent.contains(FEDORA)) {
            return OperatingSystem.FEDORA;
        }

        if (userAgent.contains(CENTOS)) {
            return OperatingSystem.CENTOS;
        }

        if (userAgent.contains(DEBIAN)) {
            return OperatingSystem.DEBIAN;
        }

        if (userAgent.contains(FREEBSD)) {
            return OperatingSystem.FREEBSD;
        }

        if (userAgent.contains(CROS) || userAgent.contains(CHROMEOS)) {
            return OperatingSystem.CHROME_OS;
        }

        if (userAgent.contains(LINUX)) {
            return OperatingSystem.LINUX;
        }

        return OperatingSystem.UNKNOWN;
    }
}