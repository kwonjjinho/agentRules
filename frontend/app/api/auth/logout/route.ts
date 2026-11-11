import { NextResponse } from 'next/server';
import { cookies } from 'next/headers';
import { clearAuthCookies } from '@/lib/auth/cookies';

export async function POST() {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get('accessToken')?.value;
  const backendUrl = process.env.INTERNAL_GATEWAY_URL || 'http://localhost:8080';

  if (accessToken) {
    try {
      await fetch(`${backendUrl}/api/v1/auth/logout`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      });
    } catch (error) {
      console.warn('백엔드 로그아웃 블랙리스트 요청 실패 (로컬 쿠키는 즉시 파기):', error);
    }
  }

  await clearAuthCookies();

  return NextResponse.json({ loggedOut: true }, { status: 200 });
}
