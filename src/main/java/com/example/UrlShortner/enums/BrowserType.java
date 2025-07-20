package com.example.UrlShortner.enums;

public enum BrowserType {
    CHROME("Chrome"),
    FIREFOX("Firefox"),
    SAFARI("Safari"),
    EDGE("Edge"),
    OPERA("Opera"),
    INTERNET_EXPLORER("Internet Explorer"),
    SAMSUNG_BROWSER("Samsung Browser"),
    UC_BROWSER("UC Browser"),
    UNKNOWN("Unknown");

    private final String displayName;

    BrowserType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
