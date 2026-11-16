package com.company.payment.adapter.out.feign.dto;

import java.math.BigDecimal;

public record DealDto(
        String id,
        String productId,
        String title,
        BigDecimal dealPrice,
        String status,
        int targetQuantity,
        int currentQuantity,
        boolean isFallback
) {
    public static DealDto createFallback(String dealId, String reason) {
        return new DealDto(
                dealId,
                "UNKNOWN",
                "Fallback Deal Information (" + reason + ")",
                BigDecimal.ZERO,
                "UNKNOWN",
                0,
                0,
                true
        );
    }
}
