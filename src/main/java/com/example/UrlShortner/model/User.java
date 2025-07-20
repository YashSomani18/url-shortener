package com.example.UrlShortner.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
public class User extends BaseAuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_key", length = 36)
    String userKey;
    
    @Column(name = "username", unique = true, nullable = false, length = 64)
    String username;
    
    @Column(name = "email", unique = true, nullable = false, length = 255)
    String email;
    
    @Column(name = "password", nullable = false, length = 255)
    String password;
} 