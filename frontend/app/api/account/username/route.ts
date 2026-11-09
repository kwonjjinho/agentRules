import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

export async function PATCH(req: NextRequest) {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get('accessToken')?.value;

  if (!accessToken) {
    return NextResponse.json(
      { type: 'about:blank', title: 'Unauthorized', status: 401, detail: '로그인이 필요합니다.' },
      { status: 401 }
    );
  }

  try {
    const body = await req.json();

    const backendRes = await fetch(`${process.env.INTERNAL_GATEWAY_URL}/api/v1/accounts/username`, {
      method: 'PATCH',
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
    console.error('Failed to change username via BFF:', error);
    return NextResponse.json(
      { type: 'about:blank', title: 'Internal Server Error', status: 500, detail: '서버 내부 오류가 발생했습니다.' },
      { status: 500 }
    );
  }
}
