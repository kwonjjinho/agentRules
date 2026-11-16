'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  getAuthToken,
  getStoredUser,
  setAuth as saveAuthToStorage,
  clearAuth as clearAuthFromStorage,
  AUTH_STATE_CHANGE_EVENT,
  StoredUser,
} from '@/lib/auth/storage';

export function useAuth() {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<StoredUser | null>(null);
  const [isInitialized, setIsInitialized] = useState<boolean>(false);

  const syncState = useCallback(() => {
    const currentToken = getAuthToken();
    const currentUser = getStoredUser();
    setToken(currentToken);
    setUser(currentUser);
  }, []);

  useEffect(() => {
    syncState();
    setIsInitialized(true);

    const handleAuthChange = () => {
      syncState();
    };

    window.addEventListener(AUTH_STATE_CHANGE_EVENT, handleAuthChange);
    window.addEventListener('storage', handleAuthChange);

    return () => {
      window.removeEventListener(AUTH_STATE_CHANGE_EVENT, handleAuthChange);
      window.removeEventListener('storage', handleAuthChange);
    };
  }, [syncState]);

  const login = useCallback((jwtToken: string, userInfo: StoredUser) => {
    saveAuthToStorage(jwtToken, userInfo);
    setToken(jwtToken);
    setUser(userInfo);
  }, []);

  const logout = useCallback(async () => {
    try {
      await fetch('/api/auth/logout', { method: 'POST' });
    } catch (e) {
      console.warn('BFF 로그아웃 호출 실패:', e);
    }
    clearAuthFromStorage();
    setToken(null);
    setUser(null);
  }, []);

  return {
    isLoggedIn: !!token,
    user,
    token,
    isInitialized,
    login,
    logout,
  };
}
