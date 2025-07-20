package com.example.UrlShortner.controller;

import com.example.UrlShortner.dto.UserLoginRequest;
import com.example.UrlShortner.dto.UserLoginResponse;
import com.example.UrlShortner.dto.UserRegistrationRequest;
import com.example.UrlShortner.dto.UserRegistrationResponse;
import com.example.UrlShortner.dto.UrlResponse;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void register_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        UserRegistrationResponse response = UserRegistrationResponse.builder()
                .userKey("user-key-123")
                .username("testuser")
                .email("test@example.com")
                .build();

        when(userService.createUser(any(UserRegistrationRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userKey").value("user-key-123"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).createUser(any(UserRegistrationRequest.class));
    }

    @Test
    void register_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("")
                .email("invalid-email")
                .password("")
                .build();

        when(userService.createUser(any(UserRegistrationRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid request"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService).createUser(any(UserRegistrationRequest.class));
    }

    @Test
    void register_ServiceException_ReturnsInternalServerError() throws Exception {
        // Given
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        when(userService.createUser(any(UserRegistrationRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(userService).createUser(any(UserRegistrationRequest.class));
    }

    @Test
    void login_ValidRequest_ReturnsOk() throws Exception {
        // Given
        UserLoginRequest request = UserLoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        UserLoginResponse response = UserLoginResponse.builder()
                .userKey("user-key-123")
                .username("testuser")
                .email("test@example.com")
                .token("jwt-token-123")
                .build();

        when(userService.authenticateAndBuildLoginResponse(any(UserLoginRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userKey").value("user-key-123"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("jwt-token-123"));

        verify(userService).authenticateAndBuildLoginResponse(any(UserLoginRequest.class));
    }

    @Test
    void getUserInfo_ValidUsername_ReturnsOk() throws Exception {
        // Given
        String username = "testuser";
        List<UrlResponse> urls = Arrays.asList(
                UrlResponse.builder()
                        .id("url-key-1")
                        .originalUrl("https://example.com/1")
                        .shortCode("abc123")
                        .build(),
                UrlResponse.builder()
                        .id("url-key-2")
                        .originalUrl("https://example.com/2")
                        .shortCode("def456")
                        .build()
        );

        when(userService.getAllUrlsByUserName(username)).thenReturn(urls);

        // When & Then
        mockMvc.perform(get("/api/auth/user/{username}", username)
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("url-key-1"))
                .andExpect(jsonPath("$[0].originalUrl").value("https://example.com/1"))
                .andExpect(jsonPath("$[0].shortCode").value("abc123"))
                .andExpect(jsonPath("$[1].id").value("url-key-2"))
                .andExpect(jsonPath("$[1].originalUrl").value("https://example.com/2"))
                .andExpect(jsonPath("$[1].shortCode").value("def456"));

        verify(userService).getAllUrlsByUserName(username);
    }

    @Test
    void getUserInfo_ServiceException_ReturnsInternalServerError() throws Exception {
        // Given
        String username = "testuser";
        when(userService.getAllUrlsByUserName(username))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/auth/user/{username}", username)
                        .param("username", username))
                .andExpect(status().isInternalServerError());

        verify(userService).getAllUrlsByUserName(username);
    }

    @Test
    void updatePassword_ValidRequest_ReturnsOk() throws Exception {
        // Given
        String username = "testuser";
        String newPassword = "newpassword123";

        doNothing().when(userService).updatePassword(username, newPassword);

        // When & Then
        mockMvc.perform(post("/api/auth/update-password")
                        .param("username", username)
                        .param("newPassword", newPassword))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated successfully"));

        verify(userService).updatePassword(username, newPassword);
    }

    @Test
    void updatePassword_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given
        String username = "testuser";
        String newPassword = "weak";

        doThrow(new IllegalArgumentException("Password too weak"))
                .when(userService).updatePassword(username, newPassword);

        // When & Then
        mockMvc.perform(post("/api/auth/update-password")
                        .param("username", username)
                        .param("newPassword", newPassword))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password too weak"));

        verify(userService).updatePassword(username, newPassword);
    }

    @Test
    void deleteUser_ValidUserKey_ReturnsOk() throws Exception {
        // Given
        String userKey = "user-key-123";

        doNothing().when(userService).deleteUser(userKey);

        // When & Then
        mockMvc.perform(delete("/api/auth/user/{userKey}", userKey))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));

        verify(userService).deleteUser(userKey);
    }

    @Test
    void deleteUser_InvalidUserKey_ReturnsBadRequest() throws Exception {
        // Given
        String userKey = "invalid-key";

        doThrow(new IllegalArgumentException("User not found"))
                .when(userService).deleteUser(userKey);

        // When & Then
        mockMvc.perform(delete("/api/auth/user/{userKey}", userKey))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found"));

        verify(userService).deleteUser(userKey);
    }
} 