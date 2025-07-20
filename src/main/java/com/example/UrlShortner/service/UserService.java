package com.example.UrlShortner.service;

import com.example.UrlShortner.dto.*;
import com.example.UrlShortner.model.Url;
import com.example.UrlShortner.model.User;
import com.example.UrlShortner.repository.UrlRepository;
import com.example.UrlShortner.repository.UserRepository;
import com.example.UrlShortner.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final UrlRepository urlRepository;
    private final PasswordEncoder passwordEncoder;
    private final UrlService urlService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${host.link}")
    private String hostLink;
    
    @Transactional
    public UserRegistrationResponse createUser(UserRegistrationRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        User user =  User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        
        userRepository.save(user);

        UserRegistrationResponse response = UserRegistrationResponse.builder()
                .userKey(user.getUserKey())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdOn(user.getCreatedOn())
                .build();

        log.info("Created new user: {}", request.getUsername());
        return response;
    }
    
    @Transactional
    public User updateUser(String userKey, String username, String email) {
        User user = userRepository.findByUserKey(userKey);
        if (Objects.nonNull(user)) {

            if (!username.equals(user.getUsername()) && userRepository.existsByUsername(username)) {
                throw new IllegalArgumentException("Username already exists");
            }

            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already exists");
            }
            
            user.setUsername(username);
            user.setEmail(email);
            
            User updatedUser = userRepository.save(user);
            log.info("Updated user: {}", username);
            return updatedUser;
        }
        throw new IllegalArgumentException("User not found");
    }
    
    @Transactional
    public void updatePassword(UpdatePasswordRequest request) {
        // Validate that the authenticated user can only modify their own password
        validateUserAccess(request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername());
        if (Objects.isNull(user)) {
            throw new IllegalArgumentException("User not found");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from old password");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Updated password for user: {}", user.getUsername());
    }

    /**
     * Validates that the authenticated user matches the requested username
     * This ensures users can only modify their own data
     */
    public void validateUserAccess(String requestedUsername) {
        // Get the current authenticated user from Spring Security context
        String authenticatedUsername = getCurrentAuthenticatedUsername();
        
        if (authenticatedUsername == null) {
            throw new IllegalArgumentException("User not authenticated");
        }
        
        // Check if the authenticated user matches the requested username
        if (!authenticatedUsername.equals(requestedUsername)) {
            throw new IllegalArgumentException("Access denied: You can only modify your own data");
        }
    }
    
    /**
     * Gets the username of the currently authenticated user from Spring Security context
     */
    private String getCurrentAuthenticatedUsername() {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getName())) {
                return authentication.getName();
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting authenticated username", e);
            return null;
        }
    }
    
    @Transactional
    public void deleteUser(String userKey) {
        User user = userRepository.findByUserKey(userKey);
        if (Objects.nonNull(user)) {
            userRepository.deleteByUserKey(userKey);
            log.info("Deleted user: {}",user.getUsername());
        }
    }

    public User getUserByUsername(String username){
        return userRepository.findByUsername(username);
    }

    /**
     * Authenticates a user by username or email and password, generates JWT, and builds the login response DTO.
     */
    public UserLoginResponse authenticateAndBuildLoginResponse(UserLoginRequest request) {
        User user = null;

        if ( Objects.nonNull(request.getUsername()) && !request.getUsername().trim().isEmpty()) {
            user = userRepository.findByUsername(request.getUsername());
        }

        if (Objects.isNull(user) && Objects.nonNull(request.getEmail()) && !request.getEmail().trim().isEmpty()) {
            user = userRepository.findByEmail(request.getEmail()).orElse(null);
        }

        if (Objects.isNull(user) || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username/email or password");
        }
        String token = jwtTokenProvider.generateToken(user);
        return UserLoginResponse.builder()
                .userKey(user.getUserKey())
                .username(user.getUsername())
                .email(user.getEmail())
                .token(token)
                .build();
    }

    public List<UrlResponse> getAllUrlsByUserName(String username){
        User user = userRepository.findByUsername(username);
        List<Url> urls = urlRepository.findByUserKey(user.getUserKey());
        return urls.stream()
            .map(url -> urlService.getUrlResponse(url, user))
            .toList();
    }

} 