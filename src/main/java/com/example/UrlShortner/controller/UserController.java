package com.example.UrlShortner.controller;

import com.example.UrlShortner.dto.*;
import com.example.UrlShortner.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    /**
     * Registers a new user.
     * @param request User registration request body (username, email, password)
     * @return UserRegistrationResponse with user details
     */
    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponse> register(@RequestBody @Valid UserRegistrationRequest request) {
        try {
            UserRegistrationResponse response = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Registration error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error during registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Authenticates a user and returns a JWT token on success.
     * @param request User login request body (username/email, password)
     * @return UserLoginResponse with user details and JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody @Valid UserLoginRequest request) {
        UserLoginResponse response = userService.authenticateAndBuildLoginResponse(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retrieves all URLs created by a user.
     * @param username The username to fetch URLs for (as path variable)
     * @return List of UrlResponse objects for the user
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<UrlResponse>> getUserInfo(@PathVariable String username) {
        try {
            userService.validateUserAccess(username);
            
            List<UrlResponse> urls = userService.getAllUrlsByUserName(username);
            return ResponseEntity.ok(urls);
        } catch (IllegalArgumentException e) {
            log.error("Access denied for user info: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            log.error("Error getting user info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates the password for a user.
     * @param request UpdatePasswordRequest containing username, oldPassword, and newPassword
     * @return Success or error message
     */
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody @Valid UpdatePasswordRequest request) {
        try {
            userService.updatePassword(request);
            return ResponseEntity.ok("Password updated successfully");
        } catch (IllegalArgumentException e) {
            log.error("Password update error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during password update", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating password");
        }
    }

    /**
     * Deletes a user by their userKey.
     * @param userKey The unique key of the user to delete (as path variable)
     * @return Success or error message
     */
    @DeleteMapping("/user/{userKey}")
    public ResponseEntity<String> deleteUser(@PathVariable String userKey) {
        try {
            userService.deleteUser(userKey);
            return ResponseEntity.ok("User deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 