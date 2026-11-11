import { AuthForm } from '@/components/auth/AuthForm';

export default function LoginPage() {
  return (
    <main className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <span className="text-xs font-semibold uppercase tracking-widest text-indigo-600">
          Zero-Trust BFF Security
        </span>
        <h1 className="mt-2 text-3xl font-extrabold text-gray-900">
          공동구매 플랫폼 로그인
        </h1>
        <p className="mt-2 text-sm text-gray-600">
          HttpOnly 보안 쿠키 기반으로 안전하게 계정을 보호합니다.
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0">
        <AuthForm />
      </div>
    </main>
  );
}
