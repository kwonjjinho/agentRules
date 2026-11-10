package com.company.deal.adapter.in.web;

import com.company.deal.application.service.DealRealtimeStreamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DealSseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DealRealtimeStreamService realtimeStreamService;

    @InjectMocks
    private DealSseController dealSseController;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(dealSseController).build();
    }

    @Test
    @DisplayName("공동구매 실시간 SSE 스트림 구독 시 text/event-stream 응답 및 버퍼링 방지 헤더가 정상 반환된다")
    void subscribeDealStream_ReturnsEventStreamWithNoBufferingHeaders() throws Exception {
        // given
        String dealId = "DEAL-REALTIME-001";
        SseEmitter emitter = new SseEmitter(10000L);
        given(realtimeStreamService.subscribe(dealId)).willReturn(emitter);

        // when & then
        MvcResult result = mockMvc.perform(get("/api/v1/deals/{dealId}/stream", dealId)
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(header().string("Cache-Control", "no-cache, no-transform"))
                .andExpect(header().string("X-Accel-Buffering", "no"))
                .andReturn();

        verify(realtimeStreamService).subscribe(dealId);
        assertThat(result.getResponse().getContentType()).contains("text/event-stream");
    }
}
