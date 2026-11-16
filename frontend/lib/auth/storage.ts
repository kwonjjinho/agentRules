'use client';

export interface StoredUser {
  email: string;
}

export const AUTH_TOKEN_KEY = 'accessToken';
export const AUTH_USER_KEY = 'user';
export const AUTH_STATE_CHANGE_EVENT = 'dealflow_auth_change';

/**
 * 로컬스토리지에서 JWT Access Token을 가져옵니다.
 */
export function getAuthToken(): string | null {
  if (typeof window === 'undefined') return null;
  try {
    return localStorage.getItem(AUTH_TOKEN_KEY);
  } catch (e) {
    console.error('LocalStorage 접근 오류:', e);
    return null;
  }
}

/**
 * 로컬스토리지에서 저장된 유저 정보를 가져옵니다.
 */
export function getStoredUser(): StoredUser | null {
  if (typeof window === 'undefined') return null;
  try {
    const raw = localStorage.getItem(AUTH_USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (e) {
    console.error('LocalStorage 사용자 정보 파싱 오류:', e);
    return null;
  }
}

/**
 * 로그인 성공 시 토큰과 사용자 정보를 로컬스토리지에 저장하고
 * 전체 클라이언트 컴포넌트에 상태 변경 이벤트를 발행합니다.
 */
export function setAuth(token: string, user: StoredUser): void {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(AUTH_TOKEN_KEY, token);
    localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user));
    window.dispatchEvent(new CustomEvent(AUTH_STATE_CHANGE_EVENT, { detail: { token, user } }));
  } catch (e) {
    console.error('LocalStorage 저장 오류:', e);
  }
}

/**
 * 로그아웃 시 로컬스토리지 인증 정보를 제거하고 상태 변경 이벤트를 발행합니다.
 */
export function clearAuth(): void {
  if (typeof window === 'undefined') return;
  try {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    localStorage.removeItem(AUTH_USER_KEY);
    window.dispatchEvent(new CustomEvent(AUTH_STATE_CHANGE_EVENT, { detail: null }));
  } catch (e) {
    console.error('LocalStorage 삭제 오류:', e);
  }
}
