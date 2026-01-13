// 비로그인 사용자: 게시글 조회(GET) 가능
// 로그인 사용자: 게시글 작성/수정/삭제(POST/PATCH/DELETE) 가능
// JWT 토큰 인증 적용

package com.somshare.somshare.config;

import com.somshare.somshare.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // OPTIONS(프리플라이트) 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // H2 콘솔 허용
                        .requestMatchers("/h2-console/**").permitAll()

                        // Swagger 허용
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // 인증 API 허용 (로그인, 회원가입, 이메일 인증)
                        .requestMatchers("/api/auth/**").permitAll()

                        // 사용자 정보 조회는 로그인 필요
                        .requestMatchers("/api/users/me").authenticated()

                        // 파일 다운로드 허용
                        .requestMatchers(HttpMethod.GET, "/api/files/**").permitAll()

                        // 족보 업로드는 로그인 필요
                        .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/files/**").authenticated()

                        // 게시글 작성/수정/삭제 로그인 필요
                        .requestMatchers(HttpMethod.POST, "/departments/*/exam-posts").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/departments/*/exam-posts/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/departments/*/exam-posts/*").authenticated()

                        // 조회(GET)는 누구나 가능
                        .anyRequest().permitAll()
                )

                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
