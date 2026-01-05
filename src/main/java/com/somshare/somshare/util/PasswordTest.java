package com.somshare.somshare.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordTest {

    @Bean
    CommandLineRunner passwordRunner() {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String encoded = encoder.encode("password123");
            System.out.println("BCrypt password = " + encoded);
        };
    }
}
