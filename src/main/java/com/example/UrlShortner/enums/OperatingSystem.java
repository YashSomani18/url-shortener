package com.example.UrlShortner.enums;

public enum OperatingSystem {
    WINDOWS("Windows"),
    MACOS("macOS"),
    LINUX("Linux"),
    ANDROID("Android"),
    IOS("iOS"),
    UBUNTU("Ubuntu"),
    FEDORA("Fedora"),
    CENTOS("CentOS"),
    DEBIAN("Debian"),
    FREEBSD("FreeBSD"),
    CHROME_OS("Chrome OS"),
    UNKNOWN("Unknown");

    private final String displayName;

    OperatingSystem(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 