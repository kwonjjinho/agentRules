package com.company.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 실리콘밸리 Zero-Trust 보안 표준 JWT 발급 및 검증 프로바이더.
 * - Access Token: 15분 단기 수명, sub(userId), roles, jti(블랙리스트 식별자) 포함.
 * - Refresh Token: 7일 장기 수명, Redis Rotation 검증용 jti 포함.
 */
@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-ms:900000}") long accessExpirationMs,
            @Value("${jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(String userId, String rolesCsv) {
        long now = System.currentTimeMillis();
        List<String> roles = Arrays.stream(rolesCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .claim("roles", roles)
                .claim("token_type", "access")
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessExpirationMs))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .claim("token_type", "refresh")
                .issuedAt(new Date(now))
                .expiration(new Date(now + refreshExpirationMs))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getRemainingExpirationMillis(Claims claims) {
        Date expiration = claims.getExpiration();
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0L);
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }
}
