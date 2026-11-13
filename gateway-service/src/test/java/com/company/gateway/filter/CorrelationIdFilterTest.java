package com.company.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;

    @BeforeEach
    void setUp() {
        this.filter = new CorrelationIdFilter();
    }

    @Test
    @DisplayName("인바운드 요청에 X-Correlation-Id 헤더가 없으면 새 UUID를 생성하여 다운스트림 및 응답 헤더에 전파한다")
    void shouldGenerateCorrelationIdWhenMissing() {
        // given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/deals").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<ServerWebExchange> downstreamExchange = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            downstreamExchange.set(ex);
            return Mono.empty();
        };

        // when
        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        // then
        String reqCorrelationId = downstreamExchange.get().getRequest().getHeaders()
                .getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);
        String resCorrelationId = exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);

        assertThat(reqCorrelationId).isNotBlank();
        assertThat(resCorrelationId).isEqualTo(reqCorrelationId);
    }

    @Test
    @DisplayName("인바운드 요청에 기존 X-Correlation-Id가 존재하면 이를 덮어쓰지 않고 보존하여 전파한다")
    void shouldPreserveExistingCorrelationId() {
        // given
        String existingTraceId = "trace-external-12345-abcde";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/deals")
                .header(CorrelationIdFilter.CORRELATION_ID_HEADER, existingTraceId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<ServerWebExchange> downstreamExchange = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            downstreamExchange.set(ex);
            return Mono.empty();
        };

        // when
        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        // then
        String reqCorrelationId = downstreamExchange.get().getRequest().getHeaders()
                .getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);
        String resCorrelationId = exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);

        assertThat(reqCorrelationId).isEqualTo(existingTraceId);
        assertThat(resCorrelationId).isEqualTo(existingTraceId);
    }
}
