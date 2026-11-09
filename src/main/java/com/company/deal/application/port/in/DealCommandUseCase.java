package com.company.deal.application.port.in;

import com.company.deal.adapter.in.web.dto.CreateDealRequest;
import com.company.deal.adapter.in.web.dto.DealResponse;
import com.company.deal.adapter.in.web.dto.JoinDealRequest;

public interface DealCommandUseCase {
    DealResponse createDeal(CreateDealRequest request);
    void openDeal(String dealId);
    void joinDeal(String dealId, String userId, JoinDealRequest request);
    void closeDeal(String dealId);
}
