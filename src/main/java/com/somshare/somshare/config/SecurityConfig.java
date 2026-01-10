// 비로그인 사용자: 게시글 조회(GET) 가능
// 로그인 사용자: 게시글 작성/수정/삭제(POST/PATCH/DELETE) 가능
// 권한이 분리된 SecurityConfig (전체 permitAll 아님)
// 개발/테스트용 Basic Auth 계정: user / 1234

package com.somshare.somshare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * ✅ 개발/테스트용 로그인 계정
     * Swagger Authorize에서 user / 1234 로 로그인 가능
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("user")
                .password(passwordEncoder.encode("1234")) // ✅ BCrypt 사용(PasswordEncoderConfig의 빈)
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                .authorizeHttpRequests(auth -> auth
                        // ✅ OPTIONS(프리플라이트) 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ H2 콘솔 허용
                        .requestMatchers("/h2-console/**").permitAll()

                        // ✅ Swagger 허용
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // ✅ 게시글 작성/수정/삭제만 로그인 필요
                        .requestMatchers(HttpMethod.POST, "/departments/*/exam-posts").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/departments/*/exam-posts/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/departments/*/exam-posts/*").authenticated()

                        // ✅ 조회(GET)는 누구나 가능
                        .anyRequest().permitAll()
                )

                // ✅ Basic Auth 켜기 (Swagger/curl에서 테스트용)
                .httpBasic(basic -> {});

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
