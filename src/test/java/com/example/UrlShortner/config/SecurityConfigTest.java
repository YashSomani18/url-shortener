package com.example.UrlShortner.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void securityConfig_LoadsSuccessfully() {
        // Verify that the security configuration loads successfully
        assertThat(applicationContext).isNotNull();
        
        // Verify that security-related beans are present
        assertThat(applicationContext.containsBean("springSecurityFilterChain")).isTrue();
    }

    @Test
    void securityBeans_AreConfigured() {
        // Test that essential security beans are configured
        assertThat(applicationContext.getBeansOfType(org.springframework.security.crypto.password.PasswordEncoder.class))
                .isNotEmpty();
        
        assertThat(applicationContext.getBeansOfType(org.springframework.security.authentication.AuthenticationManager.class))
                .isNotEmpty();
    }

    @Test
    void jwtTokenProvider_IsConfigured() {
        // Test that JWT token provider is configured
        assertThat(applicationContext.getBeansOfType(JwtTokenProvider.class))
                .isNotEmpty();
    }

    @Test
    void redisConfig_IsConfigured() {
        // Test that Redis configuration is present
        assertThat(applicationContext.getBeansOfType(org.springframework.data.redis.connection.RedisConnectionFactory.class))
                .isNotEmpty();
    }
} 