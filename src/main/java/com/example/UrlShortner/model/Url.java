package com.example.UrlShortner.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "urls")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder  // Change from @Builder to @SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)  // Change to true since you extend BaseAuditableEntity
public class Url extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "url_key", length = 36)
    String urlKey;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    String originalUrl;

    @Column(name = "short_code", unique = true, nullable = false, length = 10)
    String shortCode;

    @Column(name = "expires_at")
    LocalDateTime expiresAt;

    @Column(name = "click_count", nullable = false)
    @Builder.Default  // Add this for default value
    Long clickCount = 0L;

    @Column(name = "user_key", nullable = true, length = 36)
    String userKey;

    @Column(name = "is_active", nullable = false)
    @Builder.Default  // Add this for default value
    Boolean isActive = true;

    @Column(name = "title", length = 255)
    String title;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;
}