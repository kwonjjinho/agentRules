package com.company.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 실리콘밸리 엔터프라이즈 MSA 분산 추적(Observability) 글로벌 필터.
 * 모든 외부 요청 진입점에서 고유 추적 ID('X-Correlation-Id')를 생성 또는 보존하여
 * 다운스트림 마이크로서비스 요청 및 클라이언트 응답 헤더에 전파합니다.
 */
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String existingId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        String correlationId = (existingId != null && !existingId.isBlank())
                ? existingId
                : UUID.randomUUID().toString();

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build();

        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);

        log.debug("Propagating {}={} for {} {}", CORRELATION_ID_HEADER, correlationId,
                request.getMethod(), request.getURI().getPath());

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
