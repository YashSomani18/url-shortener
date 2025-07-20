package com.example.UrlShortner.controller;

import com.example.UrlShortner.dto.*;
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
                .username("testuser")
                .email("invalid-email")
                .password("password123")
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
    void login_WithEmail_ReturnsOk() throws Exception {
        // Given
        UserLoginRequest request = UserLoginRequest.builder()
                .email("test@example.com")
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
                        .shortCode("abc123")
                        .originalUrl("https://example.com/1")
                        .shortUrl("http://localhost:8081/abc123")
                        .title("Example 1")
                        .description("First example")
                        .build(),
                UrlResponse.builder()
                        .shortCode("def456")
                        .originalUrl("https://example.com/2")
                        .shortUrl("http://localhost:8081/def456")
                        .title("Example 2")
                        .description("Second example")
                        .build()
        );

        when(userService.getAllUrlsByUserName(username)).thenReturn(urls);

        // When & Then
        mockMvc.perform(get("/api/auth/user/{username}", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shortCode").value("abc123"))
                .andExpect(jsonPath("$[0].originalUrl").value("https://example.com/1"))
                .andExpect(jsonPath("$[0].shortUrl").value("http://localhost:8081/abc123"))
                .andExpect(jsonPath("$[0].title").value("Example 1"))
                .andExpect(jsonPath("$[1].shortCode").value("def456"))
                .andExpect(jsonPath("$[1].originalUrl").value("https://example.com/2"))
                .andExpect(jsonPath("$[1].shortUrl").value("http://localhost:8081/def456"))
                .andExpect(jsonPath("$[1].title").value("Example 2"));

        verify(userService).getAllUrlsByUserName(username);
    }

    @Test
    void getUserInfo_AccessDenied_ReturnsForbidden() throws Exception {
        // Given
        String username = "testuser";
        when(userService.getAllUrlsByUserName(username))
                .thenThrow(new IllegalArgumentException("Access denied: You can only modify your own data"));

        // When & Then
        mockMvc.perform(get("/api/auth/user/{username}", username))
                .andExpect(status().isForbidden());

        verify(userService).getAllUrlsByUserName(username);
    }

    @Test
    void getUserInfo_ServiceException_ReturnsInternalServerError() throws Exception {
        // Given
        String username = "testuser";
        when(userService.getAllUrlsByUserName(username))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/auth/user/{username}", username))
                .andExpect(status().isInternalServerError());

        verify(userService).getAllUrlsByUserName(username);
    }

    @Test
    void updatePassword_ValidRequest_ReturnsOk() throws Exception {
        // Given
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .username("testuser")
                .oldPassword("oldpassword123")
                .newPassword("newpassword123")
                .build();

        doNothing().when(userService).updatePassword(any(UpdatePasswordRequest.class));

        // When & Then
        mockMvc.perform(post("/api/auth/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated successfully"));

        verify(userService).updatePassword(any(UpdatePasswordRequest.class));
    }

    @Test
    void updatePassword_InvalidOldPassword_ReturnsBadRequest() throws Exception {
        // Given
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .username("testuser")
                .oldPassword("wrongpassword")
                .newPassword("newpassword123")
                .build();

        doThrow(new IllegalArgumentException("Old password is incorrect"))
                .when(userService).updatePassword(any(UpdatePasswordRequest.class));

        // When & Then
        mockMvc.perform(post("/api/auth/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Old password is incorrect"));

        verify(userService).updatePassword(any(UpdatePasswordRequest.class));
    }

    @Test
    void updatePassword_SamePassword_ReturnsBadRequest() throws Exception {
        // Given
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .username("testuser")
                .oldPassword("password123")
                .newPassword("password123")
                .build();

        doThrow(new IllegalArgumentException("New password must be different from old password"))
                .when(userService).updatePassword(any(UpdatePasswordRequest.class));

        // When & Then
        mockMvc.perform(post("/api/auth/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("New password must be different from old password"));

        verify(userService).updatePassword(any(UpdatePasswordRequest.class));
    }

    @Test
    void updatePassword_UserNotFound_ReturnsBadRequest() throws Exception {
        // Given
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .username("nonexistentuser")
                .oldPassword("password123")
                .newPassword("newpassword123")
                .build();

        doThrow(new IllegalArgumentException("User not found"))
                .when(userService).updatePassword(any(UpdatePasswordRequest.class));

        // When & Then
        mockMvc.perform(post("/api/auth/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found"));

        verify(userService).updatePassword(any(UpdatePasswordRequest.class));
    }

    @Test
    void updatePassword_AccessDenied_ReturnsBadRequest() throws Exception {
        // Given
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .username("otheruser")
                .oldPassword("password123")
                .newPassword("newpassword123")
                .build();

        doThrow(new IllegalArgumentException("Access denied: You can only modify your own data"))
                .when(userService).updatePassword(any(UpdatePasswordRequest.class));

        // When & Then
        mockMvc.perform(post("/api/auth/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Access denied: You can only modify your own data"));

        verify(userService).updatePassword(any(UpdatePasswordRequest.class));
    }

    @Test
    void updatePassword_ValidationError_ReturnsBadRequest() throws Exception {
        // Given - Missing required fields
        String invalidRequest = "{\"username\":\"testuser\"}";

        // When & Then
        mockMvc.perform(post("/api/auth/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updatePassword(any(UpdatePasswordRequest.class));
    }

    @Test
    void updatePassword_ShortPassword_ReturnsBadRequest() throws Exception {
        // Given
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .username("testuser")
                .oldPassword("password123")
                .newPassword("123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updatePassword(any(UpdatePasswordRequest.class));
    }

    @Test
    void updatePassword_ServiceException_ReturnsInternalServerError() throws Exception {
        // Given
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .username("testuser")
                .oldPassword("password123")
                .newPassword("newpassword123")
                .build();

        doThrow(new RuntimeException("Database error"))
                .when(userService).updatePassword(any(UpdatePasswordRequest.class));

        // When & Then
        mockMvc.perform(post("/api/auth/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred while updating password"));

        verify(userService).updatePassword(any(UpdatePasswordRequest.class));
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