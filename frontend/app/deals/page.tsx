import { DealResponse } from '@/lib/types/deal';
import { DealCard } from '@/components/deals/DealCard';

async function getActiveDeals(): Promise<DealResponse[]> {
  const backendUrl = process.env.INTERNAL_GATEWAY_URL || 'http://localhost:8080';

  try {
    const res = await fetch(`${backendUrl}/api/v1/deals/active`, {
      next: { revalidate: 10 }, // 10초 주기 ISR 캐싱
    });

    if (!res.ok) {
      return [];
    }

    return await res.json();
  } catch (error) {
    console.error('공동구매 딜 목록 조회 실패:', error);
    return [];
  }
}

export default async function DealsPage() {
  const deals = await getActiveDeals();

  return (
    <main className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-7xl">
        <header className="mb-10 text-center">
          <span className="text-sm font-semibold uppercase tracking-wide text-indigo-600">
            Real-time Group Buying
          </span>
          <h1 className="mt-2 text-3xl font-extrabold tracking-tight text-gray-900 sm:text-4xl">
            실시간 공동구매 특가 딜
          </h1>
          <p className="mt-3 text-base text-gray-500 max-w-2xl mx-auto">
            목표 인원이 달성되면 초특가로 구매가 확정됩니다. 마감 시간 전에 서둘러 참여해 보세요!
          </p>
        </header>

        {deals.length === 0 ? (
          <div className="text-center py-20 bg-white rounded-2xl border border-gray-200">
            <p className="text-gray-500 text-lg">현재 진행 중인 공동구매 딜이 없습니다.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {deals.map((deal) => (
              <DealCard key={deal.id} deal={deal} />
            ))}
          </div>
        )}
      </div>
    </main>
  );
}
