package com.company.gateway.filter;

import com.company.gateway.security.JwtTokenValidator;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 실리콘밸리 제로 트러스트(Zero-Trust) 보안 표준에 따른 Gateway 전역 JWT 인증 및 헤더 릴레이 필터.
 * 1. 인바운드 요청의 위조 'X-User-*' 헤더를 엄격히 제거하여 Header Spoofing을 방지합니다.
 * 2. JWT 서명/만료일을 검증하고 Redis 블랙리스트(blacklist:{jti}) 등록 여부를 비동기 확인하여 로그아웃된 토큰을 차단합니다.
 * 3. 검증 통과 시 신뢰할 수 있는 사용자 정보('X-User-Id', 'X-User-Roles')를 주입하여 다운스트림으로 릴레이합니다.
 * 4. 인증 실패 시 RFC 7807 규격 ProblemDetail JSON(HTTP 401 Unauthorized)을 반환합니다.
 */
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final JwtTokenValidator tokenValidator;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(
            JwtTokenValidator tokenValidator,
            ReactiveStringRedisTemplate redisTemplate
    ) {
        super(Config.class);
        this.tokenValidator = tokenValidator;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // 1. 화이트리스트 URL 확인 (토큰 없이 허용 가능한 엔드포인트)
            boolean isWhiteListed = isWhiteListed(path, config.getWhiteListUrls());

            // 2. 인바운드 요청의 외부 위조 헤더(X-User-*) 원천 차단/제거 (Zero-Trust Anti-Spoofing)
            ServerHttpRequest.Builder requestBuilder = request.mutate();
            requestBuilder.headers(headers -> {
                headers.remove(HEADER_USER_ID);
                headers.remove(HEADER_USER_ROLES);
            });

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                if (isWhiteListed) {
                    return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
                }
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization Bearer header", path);
            }

            String token = authHeader.substring(BEARER_PREFIX.length()).trim();

            Claims claims;
            try {
                // 3. JWT 서명 및 만료일 검증
                claims = tokenValidator.parseAndValidate(token);
            } catch (Exception e) {
                log.warn("JWT authentication failed for path {}: {}", path, e.getMessage());
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized: " + e.getMessage(), path);
            }

            String jti = claims.getId();
            String userId = tokenValidator.extractUserId(claims);
            String roles = tokenValidator.extractRoles(claims);

            // 4. Redis 블랙리스트(로그아웃 폐기 여부) 비동기 조회
            Mono<Boolean> isBlacklistedMono = (jti != null && !jti.isBlank() && redisTemplate != null)
                    ? redisTemplate.hasKey(BLACKLIST_PREFIX + jti)
                    : Mono.just(false);

            return isBlacklistedMono.flatMap(isBlacklisted -> {
                if (Boolean.TRUE.equals(isBlacklisted)) {
                    log.warn("Blocked blacklisted JWT token: userId={}, jti={}, path={}", userId, jti, path);
                    return onError(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized: Token has been revoked", path);
                }

                // 5. 신뢰할 수 있는 내부 헤더 주입 (Token Relay)
                ServerHttpRequest mutatedRequest = requestBuilder
                        .header(HEADER_USER_ID, userId)
                        .header(HEADER_USER_ROLES, roles)
                        .build();

                log.debug("Authenticated request for userId={} with roles={} to path={}", userId, roles, path);
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            });
        };
    }

    private boolean isWhiteListed(String path, List<String> whiteListUrls) {
        if (whiteListUrls == null || whiteListUrls.isEmpty()) {
            return false;
        }
        return whiteListUrls.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * RFC 7807 호환 Problem Details 에러 응답 생성
     */
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String detail, String path) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.parseMediaType("application/problem+json"));

        String errorResponseJson = String.format(
                "{\"type\":\"about:blank\",\"title\":\"%s\",\"status\":%d,\"detail\":\"%s\",\"instance\":\"%s\",\"timestamp\":\"%s\"}",
                status.getReasonPhrase(),
                status.value(),
                detail.replace("\"", "\\\""),
                path,
                Instant.now().toString()
        );

        byte[] bytes = errorResponseJson.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        private List<String> whiteListUrls = new ArrayList<>();

        public List<String> getWhiteListUrls() {
            return whiteListUrls;
        }

        public void setWhiteListUrls(List<String> whiteListUrls) {
            this.whiteListUrls = whiteListUrls;
        }
    }
}
