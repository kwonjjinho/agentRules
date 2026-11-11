import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const backendUrl = process.env.INTERNAL_GATEWAY_URL || 'http://localhost:8080';

    const backendRes = await fetch(`${backendUrl}/api/v1/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    const data = await backendRes.json().catch(() => ({}));
    return NextResponse.json(data, { status: backendRes.status });
  } catch (error) {
    console.error('회원가입 BFF 처리 실패:', error);
    return NextResponse.json(
      {
        type: 'https://api.company.com/errors/internal-error',
        title: 'Internal Server Error',
        status: 500,
        detail: '회원가입 처리 중 오류가 발생했습니다.',
      },
      { status: 500 }
    );
  }
}
