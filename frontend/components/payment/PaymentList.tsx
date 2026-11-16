'use client';

import React, { useState } from 'react';
import { PaymentResponse, PaymentStatus } from '@/lib/types/payment';

interface PaymentListProps {
  initialPayments: PaymentResponse[];
}

const statusBadgeStyles: Record<PaymentStatus, { bg: string; text: string; label: string }> = {
  PENDING: { bg: 'bg-amber-50 text-amber-700 border-amber-200', text: 'text-amber-700', label: '결제 대기' },
  COMPLETED: { bg: 'bg-emerald-50 text-emerald-700 border-emerald-200', text: 'text-emerald-700', label: '결제 완료' },
  FAILED: { bg: 'bg-rose-50 text-rose-700 border-rose-200', text: 'text-rose-700', label: '결제 실패' },
  REFUNDED: { bg: 'bg-gray-100 text-gray-700 border-gray-300', text: 'text-gray-700', label: '환불 완료' },
};

export function PaymentList({ initialPayments }: PaymentListProps) {
  const [payments, setPayments] = useState<PaymentResponse[]>(initialPayments);
  const [selectedPayment, setSelectedPayment] = useState<PaymentResponse | null>(null);
  const [refundReason, setRefundReason] = useState('단순 변심');
  const [isRefunding, setIsRefunding] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleRefundSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedPayment) return;

    setIsRefunding(true);
    setErrorMessage(null);

    try {
      const res = await fetch(`/api/payments/${selectedPayment.id}/refund`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ reason: refundReason }),
      });

      const data = await res.json().catch(() => ({}));

      if (!res.ok) {
        setErrorMessage(data.detail || data.message || '환불 처리에 실패했습니다.');
        return;
      }

      // 상태 업데이트: REFUNDED로 변경
      setPayments((prev) =>
        prev.map((p) =>
          p.id === selectedPayment.id ? { ...p, status: 'REFUNDED', failureReason: refundReason } : p
        )
      );
      setSelectedPayment(null);
      setRefundReason('단순 변심');
    } catch (err) {
      console.error('환불 요청 오류:', err);
      setErrorMessage('네트워크 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.');
    } finally {
      setIsRefunding(false);
    }
  };

  if (payments.length === 0) {
    return (
      <div className="text-center py-16 bg-white rounded-2xl border border-gray-200 shadow-sm">
        <p className="text-gray-500 text-base">아직 참여한 공동구매 결제 내역이 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {errorMessage && (
        <div className="rounded-xl bg-rose-50 border border-rose-200 p-4 text-sm text-rose-700">
          {errorMessage}
        </div>
      )}

      <div className="overflow-hidden bg-white shadow-sm rounded-2xl border border-gray-200">
        <ul className="divide-y divide-gray-100">
          {payments.map((payment) => {
            const badge = statusBadgeStyles[payment.status];
            return (
              <li key={payment.id} className="p-6 hover:bg-gray-50/50 transition-colors">
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-mono font-medium text-gray-500">
                        {payment.id}
                      </span>
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${badge.bg}`}>
                        {badge.label}
                      </span>
                    </div>
                    <p className="mt-1 text-sm text-gray-600">
                      공구 딜 번호: <span className="font-semibold text-gray-900">{payment.dealId}</span>
                    </p>
                    <p className="text-xs text-gray-400 mt-0.5">
                      결제 일시: {new Date(payment.createdAt).toLocaleString('ko-KR')}
                    </p>
                    {payment.failureReason && (
                      <p className="text-xs text-rose-600 mt-1">
                        사유: {payment.failureReason}
                      </p>
                    )}
                  </div>

                  <div className="flex items-center justify-between sm:justify-end gap-4">
                    <span className="text-lg font-bold text-gray-900">
                      {payment.amount.toLocaleString('ko-KR')}원
                    </span>

                    {payment.status === 'COMPLETED' && (
                      <button
                        type="button"
                        onClick={() => setSelectedPayment(payment)}
                        className="px-3.5 py-1.5 text-xs font-semibold text-rose-700 bg-rose-50 hover:bg-rose-100 border border-rose-200 rounded-lg transition-colors"
                      >
                        환불 요청
                      </button>
                    )}
                  </div>
                </div>
              </li>
            );
          })}
        </ul>
      </div>

      {/* 환불 모달 */}
      {selectedPayment && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl border border-gray-100 animate-in fade-in zoom-in-95 duration-150">
            <h3 className="text-lg font-bold text-gray-900">결제 환불 요청</h3>
            <p className="mt-1 text-sm text-gray-500">
              결제 ID: <span className="font-mono text-gray-700">{selectedPayment.id}</span> (
              {selectedPayment.amount.toLocaleString('ko-KR')}원)
            </p>

            <form onSubmit={handleRefundSubmit} className="mt-4 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  환불 사유 선택
                </label>
                <select
                  value={refundReason}
                  onChange={(e) => setRefundReason(e.target.value)}
                  className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                >
                  <option value="단순 변심">단순 변심</option>
                  <option value="배송 지연 우려">배송 지연 우려</option>
                  <option value="주문 정보 오입력">주문 정보 오입력</option>
                  <option value="기타 사유">기타 사유</option>
                </select>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setSelectedPayment(null)}
                  disabled={isRefunding}
                  className="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
                >
                  취소
                </button>
                <button
                  type="submit"
                  disabled={isRefunding}
                  className="px-4 py-2 text-sm font-semibold text-white bg-rose-600 hover:bg-rose-700 rounded-lg transition-colors disabled:opacity-50"
                >
                  {isRefunding ? '처리 중...' : '환불 확정'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
