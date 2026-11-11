import { NextRequest, NextResponse } from 'next/server';
import { AuthTokenResponse, setAuthCookies } from '@/lib/auth/cookies';

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const backendUrl = process.env.INTERNAL_GATEWAY_URL || 'http://localhost:8080';

    const backendRes = await fetch(`${backendUrl}/api/v1/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (!backendRes.ok) {
      const errorData = await backendRes.json().catch(() => ({
        title: 'Unauthorized',
        detail: '이메일 또는 비밀번호가 올바르지 않습니다.',
      }));
      return NextResponse.json(errorData, { status: backendRes.status });
    }

    const tokens: AuthTokenResponse = await backendRes.json();
    await setAuthCookies(tokens);

    return NextResponse.json(
      { authenticated: true, expiresInSeconds: tokens.expiresInSeconds },
      { status: 200 }
    );
  } catch (error) {
    console.error('로그인 BFF 처리 실패:', error);
    return NextResponse.json(
      {
        type: 'https://api.company.com/errors/internal-error',
        title: 'Internal Server Error',
        status: 500,
        detail: '인증 서버와 통신 중 오류가 발생했습니다.',
      },
      { status: 500 }
    );
  }
}
