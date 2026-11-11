import { cookies } from 'next/headers';

export interface AuthTokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
}

const isProduction = process.env.NODE_ENV === 'production';

/**
 * 발급된 JWT Access Token 및 Refresh Token을 HttpOnly 보안 쿠키에 저장합니다.
 * 클라이언트 JavaScript(document.cookie) 접근을 차단하여 XSS 탈취를 원천 방지합니다.
 */
export async function setAuthCookies(tokens: AuthTokenResponse): Promise<void> {
  const cookieStore = await cookies();

  cookieStore.set('accessToken', tokens.accessToken, {
    httpOnly: true,
    secure: isProduction,
    sameSite: 'lax',
    path: '/',
    maxAge: tokens.expiresInSeconds || 900, // 15분
  });

  cookieStore.set('refreshToken', tokens.refreshToken, {
    httpOnly: true,
    secure: isProduction,
    sameSite: 'lax',
    path: '/api/auth', // 토큰 갱신/로그아웃 BFF 엔드포인트로만 전송 제한
    maxAge: 7 * 24 * 60 * 60, // 7일
  });
}

/**
 * 인증 쿠키를 즉시 만료(삭제)시킵니다.
 */
export async function clearAuthCookies(): Promise<void> {
  const cookieStore = await cookies();

  cookieStore.set('accessToken', '', {
    httpOnly: true,
    secure: isProduction,
    sameSite: 'lax',
    path: '/',
    maxAge: 0,
  });

  cookieStore.set('refreshToken', '', {
    httpOnly: true,
    secure: isProduction,
    sameSite: 'lax',
    path: '/api/auth',
    maxAge: 0,
  });
}
