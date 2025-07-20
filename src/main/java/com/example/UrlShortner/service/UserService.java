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
    public void updatePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username);
        if (Objects.nonNull(user)) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            log.info("Updated password for user: {}", user.getUsername());
        } else {
            throw new IllegalArgumentException("User not found");
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
     * Authenticates a user by username and email and password, generates JWT, and builds the login response DTO.
     */
    public UserLoginResponse authenticateAndBuildLoginResponse(UserLoginRequest request) {
        User user = userRepository.findByUsernameAndEmail(request.getUsername(), request.getEmail());

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