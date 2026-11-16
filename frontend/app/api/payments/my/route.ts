import { NextResponse } from 'next/server';
import { cookies } from 'next/headers';

export async function GET() {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get('accessToken')?.value;

  if (!accessToken) {
    return NextResponse.json(
      {
        title: 'Unauthorized',
        status: 401,
        detail: '로그인이 필요한 서비스입니다.',
      },
      { status: 401 }
    );
  }

  try {
    const backendUrl = process.env.INTERNAL_GATEWAY_URL || 'http://localhost:8080';
    const backendRes = await fetch(`${backendUrl}/api/v1/payments/my`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
      },
      cache: 'no-store',
    });

    if (!backendRes.ok) {
      const errorJson = await backendRes.json().catch(() => ({}));
      return NextResponse.json(errorJson, { status: backendRes.status });
    }

    const payments = await backendRes.json();
    return NextResponse.json(payments, { status: 200 });
  } catch (error) {
    console.error('내 결제 내역 BFF 조회 실패:', error);
    return NextResponse.json(
      {
        title: 'Internal Server Error',
        status: 500,
        detail: '결제 내역을 조회하는 중 일시적인 서버 통신 장애가 발생했습니다.',
      },
      { status: 500 }
    );
  }
}
