package com.company.payment.adapter.out.feign;

import com.company.payment.adapter.out.feign.dto.DealDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;

import static org.assertj.core.api.Assertions.assertThat;

class DealServiceClientFallbackTest {

    private DealServiceClientFallbackFactory fallbackFactory;

    @BeforeEach
    void setUp() {
        this.fallbackFactory = new DealServiceClientFallbackFactory();
    }

    @Test
    @DisplayName("네트워크 오류 발생 시 Resilience4j FallbackFactory가 안전한 기본 DealDto 응답을 반환한다")
    void fallbackFactory_ReturnsSafeFallbackDtoOnNetworkError() {
        // given
        String dealId = "DEAL-FAIL-001";
        Throwable networkError = new ConnectException("Connection refused to deal-service");

        // when
        DealServiceClient client = fallbackFactory.create(networkError);
        DealDto fallbackDto = client.getDeal(dealId);

        // then
        assertThat(fallbackDto).isNotNull();
        assertThat(fallbackDto.id()).isEqualTo(dealId);
        assertThat(fallbackDto.isFallback()).isTrue();
        assertThat(fallbackDto.status()).isEqualTo("UNKNOWN");
        assertThat(fallbackDto.title()).contains("Fallback Deal Information");
    }

    @Test
    @DisplayName("원인 예외가 null(서킷 브레이커 OPEN 상태)인 경우에도 Fallback이 정상 생성된다")
    void fallbackFactory_ReturnsFallbackWhenCauseIsNull() {
        // given
        String dealId = "DEAL-CIRCUIT-OPEN-001";

        // when
        DealServiceClient client = fallbackFactory.create(null);
        DealDto fallbackDto = client.getDeal(dealId);

        // then
        assertThat(fallbackDto).isNotNull();
        assertThat(fallbackDto.id()).isEqualTo(dealId);
        assertThat(fallbackDto.isFallback()).isTrue();
        assertThat(fallbackDto.title()).contains("CircuitOpen");
    }
}
