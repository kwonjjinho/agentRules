import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

interface RouteContext {
  params: Promise<{ dealId: string }>;
}

export async function POST(req: NextRequest, context: RouteContext) {
  const { dealId } = await context.params;
  const cookieStore = await cookies();
  const accessToken = cookieStore.get('accessToken')?.value;

  if (!accessToken) {
    return NextResponse.json(
      {
        type: 'https://api.company.com/errors/unauthorized',
        title: 'Unauthorized',
        status: 401,
        detail: '공동구매에 참여하려면 로그인이 필요합니다.',
      },
      { status: 401 }
    );
  }

  try {
    const body = await req.json();
    const backendUrl = process.env.INTERNAL_GATEWAY_URL || 'http://localhost:8080';

    // Spring Cloud Gateway를 통해 마이크로서비스 호출
    const backendRes = await fetch(`${backendUrl}/api/v1/deals/${dealId}/join`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`,
      },
      body: JSON.stringify(body),
    });

    if (!backendRes.ok) {
      const errorProblemDetail = await backendRes.json();
      return NextResponse.json(errorProblemDetail, { status: backendRes.status });
    }

    return new NextResponse(null, { status: 204 });
  } catch (error) {
    console.error(`공구 참여 BFF 프록시 실패 (dealId=${dealId}):`, error);
    return NextResponse.json(
      {
        type: 'https://api.company.com/errors/internal-error',
        title: 'Internal Server Error',
        status: 500,
        detail: '일시적인 서버 통신 장애가 발생했습니다. 잠시 후 다시 시도해 주세요.',
      },
      { status: 500 }
    );
  }
}
