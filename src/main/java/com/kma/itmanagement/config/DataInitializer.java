package com.kma.itmanagement.config;

import com.kma.itmanagement.model.User;
import com.kma.itmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("sog").isEmpty()) {
            User admin = new User();
            admin.setUsername("sog");
            admin.setPassword(passwordEncoder.encode("Chapo@05"));
            admin.setRole("ROLE_ADMIN");
            admin.setDepartment("IT Dept");
            userRepository.save(admin);
            System.out.println("--- We are good ---");
        }
    }
}