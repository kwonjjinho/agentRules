import { NextResponse } from 'next/server';
import { cookies } from 'next/headers';
import { AuthTokenResponse, clearAuthCookies, setAuthCookies } from '@/lib/auth/cookies';

export async function POST() {
  const cookieStore = await cookies();
  const refreshToken = cookieStore.get('refreshToken')?.value;

  if (!refreshToken) {
    return NextResponse.json(
      { title: 'Unauthorized', detail: '갱신할 Refresh Token이 존재하지 않습니다.' },
      { status: 401 }
    );
  }

  try {
    const backendUrl = process.env.INTERNAL_GATEWAY_URL || 'http://localhost:8080';
    const backendRes = await fetch(`${backendUrl}/api/v1/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken }),
    });

    if (!backendRes.ok) {
      await clearAuthCookies();
      return NextResponse.json(
        { title: 'Unauthorized', detail: '세션이 만료되었습니다. 다시 로그인해 주세요.' },
        { status: 401 }
      );
    }

    const rotatedTokens: AuthTokenResponse = await backendRes.json();
    await setAuthCookies(rotatedTokens);

    return NextResponse.json(
      { refreshed: true, expiresInSeconds: rotatedTokens.expiresInSeconds },
      { status: 200 }
    );
  } catch (error) {
    console.error('토큰 갱신 BFF 처리 실패:', error);
    return NextResponse.json(
      { title: 'Internal Server Error', detail: '토큰 갱신 중 오류가 발생했습니다.' },
      { status: 500 }
    );
  }
}
