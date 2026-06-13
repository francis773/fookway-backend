package com.restaurant.config;

import com.restaurant.model.User;
import com.restaurant.model.UserRole;
import com.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Always recreate admin user with correct password
        userRepository.findByUsername("admin").ifPresent(userRepository::delete);
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .active(true)
                .build();
        userRepository.save(admin);
        System.out.println(">>> Admin user ready (admin / admin123)");

        // Always recreate kitchen user with correct password
        userRepository.findByUsername("kitchen").ifPresent(userRepository::delete);
        User kitchen = User.builder()
                .username("kitchen")
                .password(passwordEncoder.encode("kitchen123"))
                .role(UserRole.KITCHEN)
                .active(true)
                .build();
        userRepository.save(kitchen);
        System.out.println(">>> Kitchen user ready (kitchen / kitchen123)");
    }
}
