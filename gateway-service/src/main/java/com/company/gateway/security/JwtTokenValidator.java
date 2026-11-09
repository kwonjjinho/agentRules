package com.company.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Component
public class JwtTokenValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenValidator.class);

    private final SecretKey key;

    public JwtTokenValidator(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT 서명 및 유효성을 검증하고 Claims를 반환합니다.
     */
    public Claims parseAndValidate(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature or format: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT signature or format");
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            throw new IllegalArgumentException("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            throw new IllegalArgumentException("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            log.warn("Empty or invalid JWT token string: {}", e.getMessage());
            throw new IllegalArgumentException("JWT claims string is empty or invalid");
        }
    }

    /**
     * Claims에서 사용자 ID(sub)를 추출합니다.
     */
    public String extractUserId(Claims claims) {
        return claims.getSubject();
    }

    /**
     * Claims에서 역할(roles) 목록을 쉼표로 구분된 문자열로 추출합니다.
     */
    @SuppressWarnings("unchecked")
    public String extractRoles(Claims claims) {
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?> roleList) {
            return String.join(",", roleList.stream().map(Object::toString).toList());
        } else if (rolesObj instanceof String rolesStr) {
            return rolesStr;
        }
        return "ROLE_USER";
    }
}
