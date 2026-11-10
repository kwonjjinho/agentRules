'use client';

import { useEffect, useState, useCallback, useRef } from 'react';

export interface DealStreamData {
  dealId: string;
  currentStock: number;
  currentParticipants: number;
  targetQuantity: number;
  status: 'OPEN' | 'CLOSED' | 'SUCCESS' | 'CANCELLED';
  timestamp: string;
}

interface UseDealStreamOptions {
  initialData?: DealStreamData | null;
  onUpdate?: (data: DealStreamData) => void;
  enabled?: boolean;
}

/**
 * 공동구매 딜 실시간 SSE(Server-Sent Events) 구독 커스텀 훅.
 * 1. 백엔드 Spring Boot DealSseController에 연결하여 실시간 재고 및 상태를 반영합니다.
 * 2. 네트워크 끊김 시 지수 백오프(Exponential Backoff)로 자동 재연결합니다.
 * 3. 클라이언트 컴포넌트 언마운트 시 EventSource를 안전하게 close합니다.
 */
export function useDealStream(dealId: string, options: UseDealStreamOptions = {}) {
  const { initialData = null, onUpdate, enabled = true } = options;

  const [data, setData] = useState<DealStreamData | null>(initialData);
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const eventSourceRef = useRef<EventSource | null>(null);
  const retryCountRef = useRef(0);
  const retryTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const connect = useCallback(() => {
    if (typeof window === 'undefined' || !enabled || !dealId) {
      return;
    }

    // 기존 연결 정리
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    const streamUrl = `/api/v1/deals/${dealId}/stream`;
    const es = new EventSource(streamUrl);
    eventSourceRef.current = es;

    es.addEventListener('connected', () => {
      setIsConnected(true);
      setError(null);
      retryCountRef.current = 0;
    });

    es.addEventListener('snapshot', (event: MessageEvent) => {
      try {
        const payload: DealStreamData = JSON.parse(event.data);
        setData(payload);
        onUpdate?.(payload);
      } catch (err) {
        console.error('Failed to parse deal snapshot payload', err);
      }
    });

    es.addEventListener('deal-update', (event: MessageEvent) => {
      try {
        const payload: DealStreamData = JSON.parse(event.data);
        setData(payload);
        onUpdate?.(payload);
      } catch (err) {
        console.error('Failed to parse deal update payload', err);
      }
    });

    es.addEventListener('ping', () => {
      // Heartbeat ping 수신 - 연결 정상 유지 확인
      setIsConnected(true);
    });

    es.onerror = () => {
      setIsConnected(false);
      es.close();

      // 지수 백오프 재연결 (최대 30초)
      const nextDelay = Math.min(1000 * Math.pow(2, retryCountRef.current), 30000);
      retryCountRef.current += 1;

      setError(new Error(`SSE connection failed. Retrying in ${nextDelay / 1000}s...`));

      if (retryTimeoutRef.current) {
        clearTimeout(retryTimeoutRef.current);
      }
      retryTimeoutRef.current = setTimeout(() => {
        connect();
      }, nextDelay);
    };
  }, [dealId, enabled, onUpdate]);

  useEffect(() => {
    connect();

    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
        eventSourceRef.current = null;
      }
      if (retryTimeoutRef.current) {
        clearTimeout(retryTimeoutRef.current);
        retryTimeoutRef.current = null;
      }
      setIsConnected(false);
    };
  }, [connect]);

  return {
    data,
    isConnected,
    error,
    reconnect: connect,
  };
}
