package com.example.UrlShortner.repository;

import com.example.UrlShortner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    User findByUsername(String username);

    User findByUserKey(String userKey);
    
    Optional<User> findByEmail(String email);

    User findByUsernameAndEmail(String username, String email);

    void deleteByUserKey(String userKey);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
} 