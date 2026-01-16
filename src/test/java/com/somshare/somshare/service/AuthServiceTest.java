package com.somshare.somshare.service;

import com.somshare.somshare.domain.User;
import com.somshare.somshare.dto.LoginRequest;
import com.somshare.somshare.dto.LoginResponse;
import com.somshare.somshare.exception.InvalidCredentialsException;
import com.somshare.somshare.exception.UserNotFoundException;
import com.somshare.somshare.repository.EmailVerificationRepository;
import com.somshare.somshare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationRepository verificationRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private S3FileStorageService fileStorageService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtSecretKey",
            "somshare-secret-key-for-jwt-token-generation-minimum-256-bits");
        ReflectionTestUtils.setField(authService, "jwtExpiration", 86400000L);

        testUser = User.builder()
                .email("test@dongduk.ac.kr")
                .password("encodedPassword")
                .nickname("홍길동")
                .college("정보과학대학")
                .major("컴퓨터학전공")
                .points(100)
                .profileImageUrl("https://example.com/profile.jpg")
                .isVerified(true)
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }

    @Test
    @DisplayName("로그인 성공 시 토큰과 사용자 정보를 반환한다")
    void login_Success_ReturnsTokenAndUserInfo() {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@dongduk.ac.kr");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@dongduk.ac.kr"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);

        // when
        LoginResponse response = authService.login(request);

        // then
        assertThat(response.getToken()).isNotNull();
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getId()).isEqualTo(1L);
        assertThat(response.getUser().getEmail()).isEqualTo("test@dongduk.ac.kr");
        assertThat(response.getUser().getNickname()).isEqualTo("홍길동");
        assertThat(response.getUser().getCollege()).isEqualTo("정보과학대학");
        assertThat(response.getUser().getMajor()).isEqualTo("컴퓨터학전공");
        assertThat(response.getUser().getPoints()).isEqualTo(100);
        assertThat(response.getUser().getProfileImage()).isEqualTo("https://example.com/profile.jpg");
        assertThat(response.getUser().getIsVerified()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생한다")
    void login_UserNotFound_ThrowsException() {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("notexist@dongduk.ac.kr");
        request.setPassword("password123");

        when(userRepository.findByEmail("notexist@dongduk.ac.kr"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 예외가 발생한다")
    void login_InvalidPassword_ThrowsException() {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@dongduk.ac.kr");
        request.setPassword("wrongPassword");

        when(userRepository.findByEmail("test@dongduk.ac.kr"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
