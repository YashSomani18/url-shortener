package com.example.UrlShortner.enums;

public enum DeviceType {
    DESKTOP("Desktop"),
    MOBILE("Mobile"),
    TABLET("Tablet"),
    TV("Smart TV"),
    GAMING_CONSOLE("Gaming Console"),
    IOT_DEVICE("IoT Device"),
    UNKNOWN("Unknown");

    private final String displayName;

    DeviceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
