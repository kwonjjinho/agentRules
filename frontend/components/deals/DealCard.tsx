'use client';

import { useState, useEffect } from 'react';
import { DealResponse } from '@/lib/types/deal';
import { DealProgress } from './DealProgress';

interface DealCardProps {
  deal: DealResponse;
}

export function DealCard({ deal }: DealCardProps) {
  const [timeLeft, setTimeLeft] = useState<{ hours: number; minutes: number; seconds: number } | null>(null);
  const [isJoining, setIsJoining] = useState(false);
  const [quantity, setQuantity] = useState(1);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  useEffect(() => {
    const updateCountdown = () => {
      const difference = new Date(deal.endsAt).getTime() - new Date().getTime();
      if (difference <= 0) {
        setTimeLeft(null);
        return;
      }

      setTimeLeft({
        hours: Math.floor(difference / (1000 * 60 * 60)),
        minutes: Math.floor((difference / 1000 / 60) % 60),
        seconds: Math.floor((difference / 1000) % 60),
      });
    };

    updateCountdown();
    const interval = setInterval(updateCountdown, 1000);
    return () => clearInterval(interval);
  }, [deal.endsAt]);

  const handleJoin = async () => {
    setIsJoining(true);
    setErrorMsg(null);
    setSuccessMsg(null);

    try {
      const res = await fetch(`/api/deals/${deal.id}/join`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ quantity }),
      });

      if (!res.ok) {
        const errorData = await res.json();
        setErrorMsg(errorData.detail || '공동구매 참여에 실패했습니다.');
        return;
      }

      setSuccessMsg(`공동구매 ${quantity}개 참여가 완료되었습니다!`);
    } catch {
      setErrorMsg('네트워크 통신 중 오류가 발생했습니다.');
    } finally {
      setIsJoining(false);
    }
  };

  return (
    <article className="flex flex-col overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm transition hover:shadow-md">
      <div className="p-6 flex-1 flex flex-col justify-between">
        <div>
          <div className="flex items-center justify-between text-xs font-medium text-gray-500 mb-2">
            <span>상품 코드: {deal.productId}</span>
            {timeLeft ? (
              <span className="inline-flex items-center gap-1 font-mono text-red-600 bg-red-50 px-2 py-0.5 rounded">
                ⏱ {String(timeLeft.hours).padStart(2, '0')}:{String(timeLeft.minutes).padStart(2, '0')}:
                {String(timeLeft.seconds).padStart(2, '0')} 남음
              </span>
            ) : (
              <span className="text-gray-400">모집 마감</span>
            )}
          </div>

          <h3 className="text-lg font-bold text-gray-900 line-clamp-2">{deal.title}</h3>

          <div className="mt-4 flex items-baseline gap-2">
            <span className="text-2xl font-extrabold text-red-600">
              {deal.discountRate.toFixed(0)}%
            </span>
            <span className="text-2xl font-bold text-gray-900">
              {deal.dealPrice.toLocaleString()}원
            </span>
            <span className="text-sm text-gray-400 line-through">
              {deal.originalPrice.toLocaleString()}원
            </span>
          </div>

          <div className="mt-5">
            <DealProgress
              currentQuantity={deal.currentQuantity}
              targetQuantity={deal.targetQuantity}
              maxCapacity={deal.maxCapacity}
              achievementRate={deal.achievementRate}
              isAchieved={deal.isAchieved}
            />
          </div>
        </div>

        <div className="mt-6 pt-4 border-t border-gray-100">
          {errorMsg && (
            <p className="mb-2 text-xs font-medium text-red-600 bg-red-50 p-2 rounded">{errorMsg}</p>
          )}
          {successMsg && (
            <p className="mb-2 text-xs font-medium text-emerald-600 bg-emerald-50 p-2 rounded">{successMsg}</p>
          )}

          <div className="flex items-center gap-3">
            <select
              value={quantity}
              onChange={(e) => setQuantity(Number(e.target.value))}
              className="rounded-lg border border-gray-300 py-2 px-3 text-sm focus:border-indigo-500 focus:outline-none"
              disabled={isJoining || !timeLeft || deal.status === 'CLOSED'}
            >
              {[1, 2, 3, 5, 10].map((num) => (
                <option key={num} value={num}>
                  {num}개
                </option>
              ))}
            </select>

            <button
              onClick={handleJoin}
              disabled={isJoining || !timeLeft || deal.status === 'CLOSED'}
              className="flex-1 rounded-lg bg-indigo-600 py-2.5 px-4 text-center text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-indigo-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition"
            >
              {isJoining ? '처리 중...' : '지금 공구 참여하기'}
            </button>
          </div>
        </div>
      </div>
    </article>
  );
}
