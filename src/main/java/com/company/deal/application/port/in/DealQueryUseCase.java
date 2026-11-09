package com.company.deal.application.port.in;

import com.company.deal.adapter.in.web.dto.DealResponse;
import com.company.deal.domain.model.DealStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DealQueryUseCase {
    DealResponse getDeal(String dealId);
    Page<DealResponse> getDealsByStatus(DealStatus status, Pageable pageable);
    List<DealResponse> getActiveDeals();
}
