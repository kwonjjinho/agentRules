import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

interface RouteContext {
  params: Promise<{ paymentId: string }>;
}

export async function POST(req: NextRequest, context: RouteContext) {
  const { paymentId } = await context.params;
  const cookieStore = await cookies();
  let accessToken = cookieStore.get('accessToken')?.value;

  if (!accessToken) {
    const authHeader = req.headers.get('authorization');
    if (authHeader && authHeader.startsWith('Bearer ')) {
      accessToken = authHeader.substring(7);
    }
  }

  if (!accessToken) {
    return NextResponse.json(
      {
        title: 'Unauthorized',
        status: 401,
        detail: '환불을 요청하려면 로그인이 필요합니다.',
      },
      { status: 401 }
    );
  }

  try {
    const body = await req.json();
    const backendUrl = process.env.INTERNAL_GATEWAY_URL || 'http://localhost:8080';

    const backendRes = await fetch(`${backendUrl}/api/v1/payments/${paymentId}/refund`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`,
      },
      body: JSON.stringify(body),
    });

    const responseData = await backendRes.json().catch(() => ({}));

    if (!backendRes.ok) {
      return NextResponse.json(responseData, { status: backendRes.status });
    }

    return NextResponse.json(responseData, { status: 200 });
  } catch (error) {
    console.error(`결제 환불 BFF 실패 (paymentId=${paymentId}):`, error);
    return NextResponse.json(
      {
        title: 'Internal Server Error',
        status: 500,
        detail: '환불 처리 중 일시적인 서버 통신 장애가 발생했습니다.',
      },
      { status: 500 }
    );
  }
}
