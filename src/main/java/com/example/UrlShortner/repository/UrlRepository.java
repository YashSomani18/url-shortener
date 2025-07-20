package com.example.UrlShortner.repository;

import com.example.UrlShortner.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UrlRepository extends JpaRepository<Url, String> {

    Url findByShortCode(String shortCode);

    Url findByShortCodeAndIsActiveTrue(String shortCode);
    
    List<Url> findByUserKey(String userKey);
    
    List<Url> findByUserKeyOrderByCreatedOnDesc(String userKey);
    
    List<Url> findByUserKeyAndIsActiveTrueOrderByCreatedOnDesc(String userKey);
    
    List<Url> findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime now);
    
    Long countByUserKey(String userKey);

    Url findByOriginalUrlAndUserKey(String originalUrl, String userKey);
    
    boolean existsByShortCode(String shortCode);
} 