package com.company.payment.adapter.out.feign;

import com.company.payment.adapter.out.feign.dto.DealDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Resilience4j 서킷 오픈 또는 타임아웃/네트워크 오류 발생 시 동작하는 Fallback Factory.
 * 연쇄 장애(Cascading Failure)를 차단하고 안전한 Fallback 응답을 반환합니다.
 */
@Component
public class DealServiceClientFallbackFactory implements FallbackFactory<DealServiceClient> {

    private static final Logger log = LoggerFactory.getLogger(DealServiceClientFallbackFactory.class);

    @Override
    public DealServiceClient create(Throwable cause) {
        return dealId -> {
            log.error("Resilience4j Fallback triggered for getDeal(dealId={}): {}",
                    dealId, cause != null ? cause.getMessage() : "Unknown error");

            return DealDto.createFallback(dealId, cause != null ? cause.getClass().getSimpleName() : "CircuitOpen");
        };
    }
}
