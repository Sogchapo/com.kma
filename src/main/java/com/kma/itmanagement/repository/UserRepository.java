package com.kma.itmanagement.repository;

import com.kma.itmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Required for Spring Security authentication and DataInitializer check
    Optional<User> findByUsername(String username);
    
    // Helpful for checking if a user already exists during registration
    boolean existsByUsername(String username);
}