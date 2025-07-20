package com.example.UrlShortner.controller;

import com.example.UrlShortner.dto.CreateUrlRequest;
import com.example.UrlShortner.dto.UrlResponse;
import com.example.UrlShortner.model.User;
import com.example.UrlShortner.service.UrlService;
import com.example.UrlShortner.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UrlControllerTest {

    @Mock
    private UrlService urlService;

    @Mock
    private UserService userService;

    @InjectMocks
    private UrlController urlController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(urlController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createShortUrl_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        CreateUrlRequest request = CreateUrlRequest.builder()
                .originalUrl("https://example.com/very-long-url")
                .title("Test URL")
                .description("A test URL for shortening")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        UrlResponse response = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .shortUrl("http://localhost:8080/abc123")
                .title("Test URL")
                .description("A test URL for shortening")
                .isActive(true)
                .build();

        when(urlService.createShortUrl(any(CreateUrlRequest.class), anyString())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .param("username", "testuser"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("url-key-123"))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/very-long-url"))
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/abc123"))
                .andExpect(jsonPath("$.title").value("Test URL"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(urlService).createShortUrl(any(CreateUrlRequest.class), eq("testuser"));
    }

    @Test
    void createShortUrl_WithoutUsername_ReturnsCreated() throws Exception {
        // Given
        CreateUrlRequest request = CreateUrlRequest.builder()
                .originalUrl("https://example.com/very-long-url")
                .build();

        UrlResponse response = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .shortUrl("http://localhost:8080/abc123")
                .isActive(true)
                .build();

        when(urlService.createShortUrl(any(CreateUrlRequest.class), isNull())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("url-key-123"))
                .andExpect(jsonPath("$.shortCode").value("abc123"));

        verify(urlService).createShortUrl(any(CreateUrlRequest.class), isNull());
    }

    @Test
    void createShortUrl_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given
        CreateUrlRequest request = CreateUrlRequest.builder()
                .originalUrl("invalid-url")
                .build();

        when(urlService.createShortUrl(any(CreateUrlRequest.class), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid URL format"));

        // When & Then
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .param("username", "testuser"))
                .andExpect(status().isBadRequest());

        verify(urlService).createShortUrl(any(CreateUrlRequest.class), eq("testuser"));
    }

    @Test
    void createShortUrl_ServiceException_ReturnsInternalServerError() throws Exception {
        // Given
        CreateUrlRequest request = CreateUrlRequest.builder()
                .originalUrl("https://example.com/very-long-url")
                .build();

        when(urlService.createShortUrl(any(CreateUrlRequest.class), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .param("username", "testuser"))
                .andExpect(status().isInternalServerError());

        verify(urlService).createShortUrl(any(CreateUrlRequest.class), eq("testuser"));
    }

    @Test
    void getUrlInfo_ValidShortCode_ReturnsOk() throws Exception {
        // Given
        String shortCode = "abc123";
        UrlResponse response = UrlResponse.builder()
                .id("url-key-123")
                .originalUrl("https://example.com/very-long-url")
                .shortCode("abc123")
                .shortUrl("http://localhost:8080/abc123")
                .title("Test URL")
                .isActive(true)
                .build();

        when(urlService.getUrlByShortCode(shortCode)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/urls/{shortCode}", shortCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("url-key-123"))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/very-long-url"))
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(urlService).getUrlByShortCode(shortCode);
    }

    @Test
    void getUrlInfo_NotFound_ReturnsNotFound() throws Exception {
        // Given
        String shortCode = "invalid";
        when(urlService.getUrlByShortCode(shortCode)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/urls/{shortCode}", shortCode))
                .andExpect(status().isNotFound());

        verify(urlService).getUrlByShortCode(shortCode);
    }

    @Test
    void getUrlInfo_ServiceException_ReturnsInternalServerError() throws Exception {
        // Given
        String shortCode = "abc123";
        when(urlService.getUrlByShortCode(shortCode))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/urls/{shortCode}", shortCode))
                .andExpect(status().isInternalServerError());

        verify(urlService).getUrlByShortCode(shortCode);
    }

    @Test
    void getUserUrls_ValidUsername_ReturnsOk() throws Exception {
        // Given
        String username = "testuser";
        User user = User.builder()
                .userKey("user-key-123")
                .username("testuser")
                .email("test@example.com")
                .build();

        List<UrlResponse> urls = Arrays.asList(
                UrlResponse.builder()
                        .id("url-key-1")
                        .originalUrl("https://example.com/1")
                        .shortCode("abc123")
                        .isActive(true)
                        .build(),
                UrlResponse.builder()
                        .id("url-key-2")
                        .originalUrl("https://example.com/2")
                        .shortCode("def456")
                        .isActive(false)
                        .build()
        );

        when(userService.getUserByUsername(username)).thenReturn(user);
        when(urlService.getUserUrls(user.getUserKey())).thenReturn(urls);

        // When & Then
        mockMvc.perform(get("/api/urls/user/{username}", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("url-key-1"))
                .andExpect(jsonPath("$[0].originalUrl").value("https://example.com/1"))
                .andExpect(jsonPath("$[0].shortCode").value("abc123"))
                .andExpect(jsonPath("$[0].isActive").value(true))
                .andExpect(jsonPath("$[1].id").value("url-key-2"))
                .andExpect(jsonPath("$[1].originalUrl").value("https://example.com/2"))
                .andExpect(jsonPath("$[1].shortCode").value("def456"))
                .andExpect(jsonPath("$[1].isActive").value(false));

        verify(userService).getUserByUsername(username);
        verify(urlService).getUserUrls(user.getUserKey());
    }

    @Test
    void getUserUrls_UserNotFound_ReturnsBadRequest() throws Exception {
        // Given
        String username = "nonexistent";
        when(userService.getUserByUsername(username)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/urls/user/{username}", username))
                .andExpect(status().isBadRequest());

        verify(userService).getUserByUsername(username);
        verify(urlService, never()).getUserUrls(anyString());
    }

    @Test
    void getUserUrls_ServiceException_ReturnsInternalServerError() throws Exception {
        // Given
        String username = "testuser";
        when(userService.getUserByUsername(username))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/urls/user/{username}", username))
                .andExpect(status().isInternalServerError());

        verify(userService).getUserByUsername(username);
        verify(urlService, never()).getUserUrls(anyString());
    }

    @Test
    void deactivateUrl_ValidRequest_ReturnsOk() throws Exception {
        // Given
        String urlKey = "url-key-123";
        String username = "testuser";
        User user = User.builder()
                .userKey("user-key-123")
                .username("testuser")
                .build();

        when(userService.getUserByUsername(username)).thenReturn(user);
        doNothing().when(urlService).deactivateUrl(urlKey, user.getUserKey());

        // When & Then
        mockMvc.perform(delete("/api/urls/{urlKey}", urlKey)
                        .param("username", username))
                .andExpect(status().isOk());

        verify(userService).getUserByUsername(username);
        verify(urlService).deactivateUrl(urlKey, user.getUserKey());
    }

    @Test
    void deactivateUrl_UserNotFound_ReturnsBadRequest() throws Exception {
        // Given
        String urlKey = "url-key-123";
        String username = "nonexistent";

        when(userService.getUserByUsername(username)).thenReturn(null);

        // When & Then
        mockMvc.perform(delete("/api/urls/{urlKey}", urlKey)
                        .param("username", username))
                .andExpect(status().isBadRequest());

        verify(userService).getUserByUsername(username);
        verify(urlService, never()).deactivateUrl(anyString(), anyString());
    }

    @Test
    void deactivateUrl_ServiceException_ReturnsInternalServerError() throws Exception {
        // Given
        String urlKey = "url-key-123";
        String username = "testuser";
        User user = User.builder()
                .userKey("user-key-123")
                .username("testuser")
                .build();

        when(userService.getUserByUsername(username)).thenReturn(user);
        doThrow(new RuntimeException("Database error"))
                .when(urlService).deactivateUrl(urlKey, user.getUserKey());

        // When & Then
        mockMvc.perform(delete("/api/urls/{urlKey}", urlKey)
                        .param("username", username))
                .andExpect(status().isInternalServerError());

        verify(userService).getUserByUsername(username);
        verify(urlService).deactivateUrl(urlKey, user.getUserKey());
    }
} 