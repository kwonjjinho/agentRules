import './globals.css';
import Link from 'next/link';

export const metadata = {
  title: '실시간 공동구매 특가 플랫폼',
  description: 'Silicon Valley MSA 기반 실시간 공동구매 플랫폼',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <body className="min-h-screen bg-gray-50 flex flex-col">
        {/* Navigation Bar */}
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
                <Link
                  href="/login"
                  className="px-4 py-2 text-sm font-semibold text-indigo-600 hover:bg-indigo-50 rounded-xl transition-colors"
                >
                  로그인
                </Link>
              </div>
            </div>
          </div>
        </nav>

        {/* Main Content */}
        <div className="flex-1">
          {children}
        </div>

        {/* Footer */}
        <footer className="bg-white border-t border-gray-200 py-6 text-center text-xs text-gray-400">
          &copy; 2026 DealFlow Inc. Silicon Valley MSA Platform. All rights reserved.
        </footer>
      </body>
    </html>
  );
}
