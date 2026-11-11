package com.company.auth.application.service;

import com.company.auth.adapter.in.web.dto.LoginRequest;
import com.company.auth.adapter.in.web.dto.RefreshTokenRequest;
import com.company.auth.adapter.in.web.dto.RegisterRequest;
import com.company.auth.adapter.in.web.dto.TokenResponse;
import com.company.auth.domain.entity.User;
import com.company.auth.domain.repository.UserRepository;
import com.company.auth.security.JwtProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 인증 및 JWT 토큰 수명주기 관리 서비스.
 * - BCrypt 단방향 암호화
 * - Redis 기반 Refresh Token Rotation (토큰 탈취 및 재사용 방어)
 * - 로그아웃 시 Access Token JTI 기반 Redis Blacklist 등록
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "rt:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public String register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + normalizedEmail);
        }

        String userId = "USER-" + UUID.randomUUID().toString().substring(0, 8);
        String hashedPassword = passwordEncoder.encode(request.password());
        User user = new User(userId, normalizedEmail, hashedPassword, "ROLE_USER");

        userRepository.save(user);
        log.info("신규 사용자 회원가입 완료: userId={}, email={}", userId, normalizedEmail);
        return userId;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return issueTokenPairAndSaveRefresh(user.getId(), user.getRoles());
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(RefreshTokenRequest request) {
        Claims claims = jwtProvider.parseClaims(request.refreshToken());
        String tokenType = claims.get("token_type", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new IllegalArgumentException("유효한 Refresh Token이 아닙니다.");
        }

        String userId = claims.getSubject();
        String redisKey = REFRESH_TOKEN_PREFIX + userId;
        String storedToken = redisTemplate.opsForValue().get(redisKey);

        // Redis에 저장된 토큰과 불일치하면 탈취 또는 이미 사용된 토큰으로 간주하여 즉시 폐기 (Token Rotation 보안)
        if (storedToken == null || !storedToken.equals(request.refreshToken())) {
            redisTemplate.delete(redisKey);
            log.warn("Refresh Token 불일치 또는 재사용 감지 - 세션 강제 종료: userId={}", userId);
            throw new SecurityException("유효하지 않거나 만료된 Refresh Token입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 새로운 Access + Refresh Token 쌍 발급 (Refresh Token Rotation)
        return issueTokenPairAndSaveRefresh(user.getId(), user.getRoles());
    }

    public void logout(String accessToken) {
        Claims claims = jwtProvider.parseClaims(accessToken);
        String userId = claims.getSubject();
        String jti = claims.getId();
        long remainingMs = jwtProvider.getRemainingExpirationMillis(claims);

        // 1. Access Token의 남은 유효시간(TTL) 동안 Redis 블랙리스트에 등록하여 즉시 무효화
        if (remainingMs > 0 && jti != null) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + jti,
                    "revoked",
                    remainingMs,
                    TimeUnit.MILLISECONDS
            );
        }

        // 2. Refresh Token 삭제
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        log.info("사용자 로그아웃 및 토큰 블랙리스트 등록 완료: userId={}, jti={}", userId, jti);
    }

    private TokenResponse issueTokenPairAndSaveRefresh(String userId, String roles) {
        String accessToken = jwtProvider.generateAccessToken(userId, roles);
        String refreshToken = jwtProvider.generateRefreshToken(userId);

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                jwtProvider.getRefreshExpirationMs(),
                TimeUnit.MILLISECONDS
        );

        return TokenResponse.of(accessToken, refreshToken, 900);
    }
}
