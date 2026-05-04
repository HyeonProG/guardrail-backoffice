import { Navigate, useSearchParams } from 'react-router-dom';
import { LoginForm } from '@/features/auth/ui/LoginForm';
import { authStorage } from '@/features/auth/model/authStorage';
import { env } from '@/shared/config/env';

export function LoginPage() {
  const [searchParams] = useSearchParams();

  if (authStorage.getAccessToken()) {
    return <Navigate to="/dashboard" replace />;
  }

  const sessionExpired = searchParams.get('reason') === 'session-expired';

  return (
    <main className="flex min-h-screen items-center justify-center bg-surface px-4 py-10">
      <section className="w-full max-w-[420px] rounded-lg border border-border bg-white p-8 shadow-sm">
        <div className="mb-8">
          <p className="text-sm font-semibold text-slate-500">{env.appName}</p>
          <h1 className="mt-2 text-2xl font-semibold text-ink">관리자 로그인</h1>
        </div>
        {sessionExpired ? (
          <div className="mb-6 rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
            로그인 상태가 만료되었습니다. 다시 로그인해 주세요.
          </div>
        ) : null}
        <LoginForm />
      </section>
    </main>
  );
}
