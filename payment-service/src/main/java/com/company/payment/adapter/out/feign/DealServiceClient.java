package com.company.payment.adapter.out.feign;

import com.company.payment.adapter.out.feign.dto.DealDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Deal 마이크로서비스 동기 통신 OpenFeign 클라이언트.
 * Eureka 서비스 디스커버리('deal-service')를 통해 라우팅되며,
 * Resilience4j Circuit Breaker 및 FallbackFactory로 보호됩니다.
 */
@FeignClient(
        name = "deal-service",
        fallbackFactory = DealServiceClientFallbackFactory.class
)
public interface DealServiceClient {

    @GetMapping("/api/v1/deals/{dealId}")
    DealDto getDeal(@PathVariable("dealId") String dealId);
}
