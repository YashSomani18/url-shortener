package com.example.UrlShortner.controller;

import com.example.UrlShortner.dto.UrlResponse;
import com.example.UrlShortner.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RedirectControllerTest {

    @Mock
    private UrlService urlService;

    @InjectMocks
    private RedirectController redirectController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(redirectController).build();
    }

    @Test
    void redirect_ValidShortCode_ReturnsFound() throws Exception {
        // Given
        String shortCode = "abc123";
        String originalUrl = "https://example.com/very-long-url";
        
        UrlResponse response = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .isActive(true)
                .build();

        when(urlService.getClientIpAddress(any())).thenReturn("192.168.1.1");
        when(urlService.redirectToOriginalUrl(eq(shortCode), anyString(), anyString(), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/{shortCode}", shortCode)
                        .header("User-Agent", "Mozilla/5.0")
                        .header("Referer", "https://google.com"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));

        verify(urlService).getClientIpAddress(any());
        verify(urlService).redirectToOriginalUrl(eq(shortCode), eq("192.168.1.1"), 
                eq("Mozilla/5.0"), eq("https://google.com"));
    }

    @Test
    void redirect_ValidShortCodeWithoutHeaders_ReturnsFound() throws Exception {
        // Given
        String shortCode = "abc123";
        String originalUrl = "https://example.com/very-long-url";
        
        UrlResponse response = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .isActive(true)
                .build();

        when(urlService.getClientIpAddress(any())).thenReturn("192.168.1.1");
        when(urlService.redirectToOriginalUrl(eq(shortCode), anyString(), isNull(), isNull()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));

        verify(urlService).getClientIpAddress(any());
        verify(urlService).redirectToOriginalUrl(eq(shortCode), eq("192.168.1.1"), 
                isNull(), isNull());
    }

    @Test
    void redirect_ExpiredUrl_ReturnsGone() throws Exception {
        // Given
        String shortCode = "expired";
        
        UrlResponse response = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl("https://example.com/expired")
                .shortCode(shortCode)
                .isActive(false)
                .build();

        when(urlService.getClientIpAddress(any())).thenReturn("192.168.1.1");
        when(urlService.redirectToOriginalUrl(eq(shortCode), anyString(), anyString(), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/{shortCode}", shortCode)
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isGone());

        verify(urlService).getClientIpAddress(any());
        verify(urlService).redirectToOriginalUrl(eq(shortCode), eq("192.168.1.1"), 
                eq("Mozilla/5.0"), isNull());
    }

    @Test
    void redirect_NotFound_ReturnsNotFound() throws Exception {
        // Given
        String shortCode = "nonexistent";

        when(urlService.getClientIpAddress(any())).thenReturn("192.168.1.1");
        when(urlService.redirectToOriginalUrl(eq(shortCode), anyString(), anyString(), anyString()))
                .thenReturn(null);

        // When & Then
        mockMvc.perform(get("/{shortCode}", shortCode)
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isNotFound());

        verify(urlService).getClientIpAddress(any());
        verify(urlService).redirectToOriginalUrl(eq(shortCode), eq("192.168.1.1"), 
                eq("Mozilla/5.0"), isNull());
    }

    @Test
    void redirect_ServiceException_ReturnsNotFound() throws Exception {
        // Given
        String shortCode = "error";

        when(urlService.getClientIpAddress(any())).thenReturn("192.168.1.1");
        when(urlService.redirectToOriginalUrl(eq(shortCode), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/{shortCode}", shortCode)
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isNotFound());

        verify(urlService).getClientIpAddress(any());
        verify(urlService).redirectToOriginalUrl(eq(shortCode), eq("192.168.1.1"), 
                eq("Mozilla/5.0"), isNull());
    }

    @Test
    void redirect_WithXForwardedFor_ReturnsFound() throws Exception {
        // Given
        String shortCode = "abc123";
        String originalUrl = "https://example.com/very-long-url";
        
        UrlResponse response = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .isActive(true)
                .build();

        when(urlService.getClientIpAddress(any())).thenReturn("203.0.113.1");
        when(urlService.redirectToOriginalUrl(eq(shortCode), anyString(), anyString(), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/{shortCode}", shortCode)
                        .header("X-Forwarded-For", "203.0.113.1")
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));

        verify(urlService).getClientIpAddress(any());
        verify(urlService).redirectToOriginalUrl(eq(shortCode), eq("203.0.113.1"), 
                eq("Mozilla/5.0"), isNull());
    }

    @Test
    void redirect_WithRealIp_ReturnsFound() throws Exception {
        // Given
        String shortCode = "abc123";
        String originalUrl = "https://example.com/very-long-url";
        
        UrlResponse response = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .isActive(true)
                .build();

        when(urlService.getClientIpAddress(any())).thenReturn("198.51.100.1");
        when(urlService.redirectToOriginalUrl(eq(shortCode), anyString(), anyString(), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/{shortCode}", shortCode)
                        .header("X-Real-IP", "198.51.100.1")
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));

        verify(urlService).getClientIpAddress(any());
        verify(urlService).redirectToOriginalUrl(eq(shortCode), eq("198.51.100.1"), 
                eq("Mozilla/5.0"), isNull());
    }
} 