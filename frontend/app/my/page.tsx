import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import { PaymentResponse } from '@/lib/types/payment';
import { PaymentList } from '@/components/payment/PaymentList';
import Link from 'next/link';

async function getMyPayments(): Promise<PaymentResponse[]> {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get('accessToken')?.value;

  if (!accessToken) {
    redirect('/login');
  }

  const backendUrl = process.env.INTERNAL_GATEWAY_URL || 'http://localhost:8080';

  try {
    const res = await fetch(`${backendUrl}/api/v1/payments/my`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
      },
      cache: 'no-store',
    });

    if (!res.ok) {
      if (res.status === 401) {
        redirect('/login');
      }
      return [];
    }

    return await res.json();
  } catch (error) {
    console.error('마이페이지 결제 내역 조회 실패:', error);
    return [];
  }
}

export default async function MyPage() {
  const payments = await getMyPayments();

  return (
    <main className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-4xl">
        <header className="mb-8 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <span className="text-xs font-semibold uppercase tracking-wider text-indigo-600">
              User Dashboard
            </span>
            <h1 className="mt-1 text-3xl font-extrabold text-gray-900">
              내 참여 & 결제 내역
            </h1>
            <p className="mt-1 text-sm text-gray-500">
              참여한 공동구매 딜의 결제 승인 상태를 확인하고 환불을 관리할 수 있습니다.
            </p>
          </div>

          <Link
            href="/deals"
            className="inline-flex items-center px-4 py-2 text-sm font-semibold text-indigo-600 bg-indigo-50 hover:bg-indigo-100 rounded-xl transition-colors"
          >
            공구 둘러보기 &rarr;
          </Link>
        </header>

        <PaymentList initialPayments={payments} />
      </div>
    </main>
  );
}
