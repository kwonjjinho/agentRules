'use client';

import React from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';

export function Navbar() {
  const router = useRouter();
  const { isLoggedIn, user, isInitialized, logout } = useAuth();

  const handleLogout = async () => {
    await logout();
    router.push('/login');
    router.refresh();
  };

  return (
    <nav className="bg-white border-b border-gray-200 sticky top-0 z-40 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16 items-center">
          <div className="flex items-center gap-6">
            <Link href="/deals" className="flex items-center gap-2">
              <span className="text-xl font-extrabold text-indigo-600 tracking-tight">
                DEAL<span className="text-gray-900">FLOW</span>
              </span>
            </Link>
            <div className="hidden sm:flex sm:gap-4">
              <Link
                href="/deals"
                className="text-sm font-medium text-gray-700 hover:text-indigo-600 transition-colors"
              >
                공구 딜 목록
              </Link>
              <Link
                href="/my"
                className="text-sm font-medium text-gray-700 hover:text-indigo-600 transition-colors"
              >
                마이페이지
              </Link>
            </div>
          </div>

          <div className="flex items-center gap-3">
            {!isInitialized ? (
              // 초기 hydration 완료 전 깜빡임 방지용 플레이스홀더
              <div className="h-8 w-20 bg-gray-100 rounded-xl animate-pulse" />
            ) : isLoggedIn ? (
              <div className="flex items-center gap-3">
                <div className="flex items-center gap-2 bg-indigo-50 border border-indigo-100 px-3 py-1.5 rounded-xl text-xs font-medium text-indigo-800">
                  <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
                  <span className="font-semibold">{user?.email || '인증 사용자'}</span>
                </div>
                <button
                  type="button"
                  onClick={handleLogout}
                  className="px-3.5 py-1.5 text-xs font-semibold text-gray-700 hover:text-red-600 hover:bg-red-50 border border-gray-200 rounded-xl transition-colors"
                >
                  로그아웃
                </button>
              </div>
            ) : (
              <Link
                href="/login"
                className="px-4 py-2 text-sm font-semibold text-indigo-600 hover:bg-indigo-50 rounded-xl transition-colors"
              >
                로그인
              </Link>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
