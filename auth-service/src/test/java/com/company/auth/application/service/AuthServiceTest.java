package com.company.auth.application.service;

import com.company.auth.adapter.in.web.dto.LoginRequest;
import com.company.auth.adapter.in.web.dto.RefreshTokenRequest;
import com.company.auth.adapter.in.web.dto.RegisterRequest;
import com.company.auth.adapter.in.web.dto.TokenResponse;
import com.company.auth.domain.entity.User;
import com.company.auth.domain.repository.UserRepository;
import com.company.auth.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String SECRET = "EnterpriseGradeSiliconValleySecretKeyMustBeAtLeast256BitsLongForHmacSha256!";

    @Mock
    private UserRepository userRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private PasswordEncoder passwordEncoder;
    private JwtProvider jwtProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtProvider = new JwtProvider(SECRET, 900_000L, 604_800_000L);
        this.authService = new AuthService(userRepository, passwordEncoder, jwtProvider, redisTemplate);
    }

    @Test
    @DisplayName("회원가입 시 비밀번호가 BCrypt로 해싱되어 저장된다")
    void register_HashesPasswordAndSavesUser() {
        // given
        RegisterRequest request = new RegisterRequest("User@Example.com", "Password123!");
        given(userRepository.existsByEmail("user@example.com")).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        String userId = authService.register(request);

        // then
        assertThat(userId).startsWith("USER-");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공 시 Access/Refresh Token을 발급하고 Redis에 Refresh Token을 저장한다")
    void login_IssuesTokensAndStoresRefreshTokenInRedis() {
        // given
        String rawPassword = "Password123!";
        User user = new User("USER-100", "test@example.com", passwordEncoder.encode(rawPassword), "ROLE_USER");
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        TokenResponse response = authService.login(new LoginRequest("test@example.com", rawPassword));

        // then
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(valueOperations).set(eq("rt:USER-100"), eq(response.refreshToken()), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("재사용되었거나 변조된 Refresh Token으로 재발급 시도 시 세션을 폐기하고 SecurityException을 발생시킨다")
    void refresh_ReusedOrMismatchedToken_ThrowsSecurityExceptionAndDeletesSession() {
        // given
        String oldToken = jwtProvider.generateRefreshToken("USER-100");
        String differentStoredToken = jwtProvider.generateRefreshToken("USER-100");

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("rt:USER-100")).willReturn(differentStoredToken);

        // when & then
        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest(oldToken)))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("유효하지 않거나 만료된 Refresh Token입니다");

        verify(redisTemplate).delete("rt:USER-100");
    }

    @Test
    @DisplayName("로그아웃 시 Access Token의 JTI를 Redis 블랙리스트에 등록하고 Refresh Token을 삭제한다")
    void logout_BlacklistsAccessTokenAndDeletesRefreshToken() {
        // given
        String accessToken = jwtProvider.generateAccessToken("USER-100", "ROLE_USER");
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        authService.logout(accessToken);

        // then
        verify(valueOperations).set(anyString(), eq("revoked"), anyLong(), eq(TimeUnit.MILLISECONDS));
        verify(redisTemplate).delete("rt:USER-100");
    }
}
