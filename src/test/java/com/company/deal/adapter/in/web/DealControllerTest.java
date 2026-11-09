package com.company.deal.adapter.in.web;

import com.company.deal.adapter.in.web.dto.DealResponse;
import com.company.deal.adapter.in.web.dto.JoinDealRequest;
import com.company.deal.application.port.in.DealCommandUseCase;
import com.company.deal.application.port.in.DealQueryUseCase;
import com.company.deal.domain.model.DealStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DealController.class)
class DealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DealCommandUseCase dealCommandUseCase;

    @MockBean
    private DealQueryUseCase dealQueryUseCase;

    @Test
    @DisplayName("공구 참여 성공: Gateway가 주입한 X-User-Id 헤더와 유효한 수량이 전달되면 204 No Content를 반환한다")
    void joinDeal_Success_Returns204() throws Exception {
        String dealId = "DEAL-001";
        String userId = "USER-1002";
        JoinDealRequest request = new JoinDealRequest(2);

        mockMvc.perform(post("/api/v1/deals/{dealId}/join", dealId)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        verify(dealCommandUseCase).joinDeal(dealId, userId, request);
    }

    @Test
    @DisplayName("공구 참여 실패 (보안 검증): X-User-Id 헤더가 누락되면 400 Bad Request를 반환한다")
    void joinDeal_MissingUserHeader_Returns400() throws Exception {
        String dealId = "DEAL-001";
        JoinDealRequest request = new JoinDealRequest(2);

        mockMvc.perform(post("/api/v1/deals/{dealId}/join", dealId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("공구 참여 실패 (유효성 검증): 수량이 허용 범위를 초과(10개 초과)하면 400 Bad Request를 반환한다")
    void joinDeal_ExceedMaxPerUser_Returns400() throws Exception {
        String dealId = "DEAL-001";
        String userId = "USER-1002";
        JoinDealRequest request = new JoinDealRequest(15); // 최대 10개 제약 초과

        mockMvc.perform(post("/api/v1/deals/{dealId}/join", dealId)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("딜 단건 상세 조회: 존재하는 딜 ID로 조회 시 200 OK 및 딜 상세 정보를 반환한다")
    void getDeal_Success_Returns200() throws Exception {
        String dealId = "DEAL-001";
        DealResponse response = DealResponse.of(
            dealId,
            "PROD-01",
            "테스트 딜 상품",
            BigDecimal.valueOf(100000),
            BigDecimal.valueOf(70000),
            50,
            200,
            35,
            DealStatus.OPEN,
            Instant.now(),
            Instant.now().plusSeconds(3600)
        );

        given(dealQueryUseCase.getDeal(dealId)).willReturn(response);

        mockMvc.perform(get("/api/v1/deals/{dealId}", dealId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(dealId))
            .andExpect(jsonPath("$.title").value("테스트 딜 상품"))
            .andExpect(jsonPath("$.discountRate").value(30.0))
            .andExpect(jsonPath("$.status").value("OPEN"));
    }
}
