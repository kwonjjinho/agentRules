package com.company.gateway.filter;

import com.company.gateway.security.JwtTokenValidator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "EnterpriseGradeSiliconValleySecretKeyMustBeAtLeast256BitsLongForHmacSha256!";
    private JwtAuthenticationFilter filter;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        JwtTokenValidator validator = new JwtTokenValidator(SECRET);
        this.filter = new JwtAuthenticationFilter(validator);
        this.secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String generateToken(String subject, List<String> roles, long ttlMillis) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlMillis))
                .signWith(secretKey)
                .compact();
    }

    @Test
    @DisplayName("유효한 JWT 토큰 인입 시 'X-User-Id' 및 'X-User-Roles' 내부 헤더가 정상 릴레이된다")
    void shouldRelayTrustedHeadersWhenTokenIsValid() {
        // given
        String validToken = generateToken("user-uuid-12345", List.of("ROLE_USER", "ROLE_BUYER"), 600_000);
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/deals/deal-1/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
        GatewayFilter gatewayFilter = filter.apply(config);

        AtomicReference<ServerWebExchange> downstreamExchange = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            downstreamExchange.set(ex);
            return Mono.empty();
        };

        // when
        StepVerifier.create(gatewayFilter.filter(exchange, chain))
                .verifyComplete();

        // then
        assertThat(downstreamExchange.get()).isNotNull();
        HttpHeaders relayedHeaders = downstreamExchange.get().getRequest().getHeaders();
        assertThat(relayedHeaders.getFirst("X-User-Id")).isEqualTo("user-uuid-12345");
        assertThat(relayedHeaders.getFirst("X-User-Roles")).isEqualTo("ROLE_USER,ROLE_BUYER");
    }

    @Test
    @DisplayName("외부에서 인위적으로 주입된 X-User-Id 헤더(Spoofing 시도)는 엄격히 차단되고 JWT 클레임으로 덮어써진다")
    void shouldPreventHeaderSpoofingAndOverwriteWithJwtClaims() {
        // given
        String validToken = generateToken("real-user-777", List.of("ROLE_USER"), 600_000);
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/deals/deal-1/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .header("X-User-Id", "attacker-impersonated-user") // 공격자가 위조 주입한 헤더
                .header("X-User-Roles", "ROLE_ADMIN")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
        GatewayFilter gatewayFilter = filter.apply(config);

        AtomicReference<ServerWebExchange> downstreamExchange = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            downstreamExchange.set(ex);
            return Mono.empty();
        };

        // when
        StepVerifier.create(gatewayFilter.filter(exchange, chain))
                .verifyComplete();

        // then
        assertThat(downstreamExchange.get()).isNotNull();
        HttpHeaders relayedHeaders = downstreamExchange.get().getRequest().getHeaders();
        // 공격자가 주입한 헤더가 아닌, JWT에서 검증된 값이 다운스트림으로 전달되어야 함
        assertThat(relayedHeaders.getFirst("X-User-Id")).isEqualTo("real-user-777");
        assertThat(relayedHeaders.getFirst("X-User-Roles")).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("보호된 API에 토큰 없이 요청 인입 시 401 Unauthorized 에러 및 RFC 7807 규격으로 차단된다")
    void shouldReturn401WhenAuthorizationHeaderIsMissing() {
        // given
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/deals/deal-1/join")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
        GatewayFilter gatewayFilter = filter.apply(config);

        GatewayFilterChain chain = ex -> Mono.empty();

        // when
        StepVerifier.create(gatewayFilter.filter(exchange, chain))
                .verifyComplete();

        // then
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getContentType().toString())
                .isEqualTo("application/problem+json");
    }

    @Test
    @DisplayName("만료된 JWT 토큰 인입 시 401 Unauthorized 에러로 차단된다")
    void shouldReturn401WhenTokenIsExpired() {
        // given (만료 시간 -10초)
        String expiredToken = generateToken("expired-user", List.of("ROLE_USER"), -10_000);
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/deals/deal-1/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
        GatewayFilter gatewayFilter = filter.apply(config);

        GatewayFilterChain chain = ex -> Mono.empty();

        // when
        StepVerifier.create(gatewayFilter.filter(exchange, chain))
                .verifyComplete();

        // then
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("화이트리스트에 등록된 경로는 토큰 없이도 통과하되, 외부 X-User-* 위조 헤더는 제거된다")
    void shouldPassWhitelistUrlsWithoutTokenAndStripSpoofedHeaders() {
        // given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/deals")
                .header("X-User-Id", "malicious-header-on-public-url")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
        config.setWhiteListUrls(List.of("/api/v1/deals", "/api/v1/deals/*"));
        GatewayFilter gatewayFilter = filter.apply(config);

        AtomicReference<ServerWebExchange> downstreamExchange = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            downstreamExchange.set(ex);
            return Mono.empty();
        };

        // when
        StepVerifier.create(gatewayFilter.filter(exchange, chain))
                .verifyComplete();

        // then
        assertThat(downstreamExchange.get()).isNotNull();
        // 공개 엔드포인트이므로 토큰 없이 통과되나, 위조 시도된 X-User-Id 헤더는 반드시 제거되어야 함
        assertThat(downstreamExchange.get().getRequest().getHeaders().getFirst("X-User-Id")).isNull();
    }
}
