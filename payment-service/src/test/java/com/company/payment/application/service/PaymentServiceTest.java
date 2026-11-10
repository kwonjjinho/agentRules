package com.company.payment.application.service;

import com.company.payment.adapter.in.web.dto.PaymentResponse;
import com.company.payment.domain.entity.Payment;
import com.company.payment.domain.model.PaymentStatus;
import com.company.payment.domain.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("신규 결제 요청 시 PG사 승인을 획득하고 COMPLETED 상태로 정상 영속화된다")
    void processPayment_Success() {
        // given
        String dealId = "DEAL-100";
        String userId = "USER-100";
        BigDecimal amount = BigDecimal.valueOf(50000);
        String idempotencyKey = "idemp-unique-001";

        given(paymentRepository.findByIdempotencyKey(idempotencyKey)).willReturn(Optional.empty());
        given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        PaymentResponse response = paymentService.processPayment(dealId, userId, amount, idempotencyKey);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(response.pgTransactionId()).startsWith("PG-");
        assertThat(response.amount()).isEqualByComparingTo(amount);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("동일한 멱등키(idempotencyKey)로 중복 결제 요청 인입 시 기존 결제를 반환하여 이중 결제를 차단한다")
    void processPayment_DuplicateIdempotencyKey_ReturnsExisting() {
        // given
        String idempotencyKey = "idemp-duplicate-001";
        Payment existing = new Payment("PAY-EXISTING", "DEAL-100", "USER-100", BigDecimal.valueOf(50000), idempotencyKey);
        existing.complete("PG-EXISTING-TX");

        given(paymentRepository.findByIdempotencyKey(idempotencyKey)).willReturn(Optional.of(existing));

        // when
        PaymentResponse response = paymentService.processPayment("DEAL-100", "USER-100", BigDecimal.valueOf(50000), idempotencyKey);

        // then
        assertThat(response.id()).isEqualTo("PAY-EXISTING");
        assertThat(response.pgTransactionId()).isEqualTo("PG-EXISTING-TX");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("본인의 완료된 결제 건에 대해 정상적으로 환불 처리된다")
    void refundPayment_Success() {
        // given
        String paymentId = "PAY-REFUND-001";
        String userId = "USER-100";
        Payment payment = new Payment(paymentId, "DEAL-100", userId, BigDecimal.valueOf(30000), "idemp-refund");
        payment.complete("PG-TX-123");

        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

        // when
        PaymentResponse response = paymentService.refundPayment(paymentId, userId, "단순 변심");

        // then
        assertThat(response.status()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(response.failureReason()).isEqualTo("단순 변심");
    }

    @Test
    @DisplayName("타인의 결제 건을 환불 시도할 경우 SecurityException 예외가 발생하며 차단된다 (IDOR 방어)")
    void refundPayment_OtherUser_ThrowsSecurityException() {
        // given
        String paymentId = "PAY-OTHER-001";
        Payment payment = new Payment(paymentId, "DEAL-100", "VICTIM-USER", BigDecimal.valueOf(30000), "idemp-other");
        payment.complete("PG-TX-123");

        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> paymentService.refundPayment(paymentId, "ATTACKER-USER", "악의적 환불 시도"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("본인의 결제만 환불 요청할 수 있습니다");
    }
}
