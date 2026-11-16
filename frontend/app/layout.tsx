import './globals.css';
import { Navbar } from '@/components/layout/Navbar';

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
        <Navbar />

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
