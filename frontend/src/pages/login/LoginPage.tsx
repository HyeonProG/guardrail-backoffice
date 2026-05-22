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
    <main className="relative flex min-h-screen items-center justify-center overflow-hidden bg-[image:var(--gradient-app)] px-4 py-10 text-[color:var(--color-text)]">
      <div className="absolute inset-0 opacity-70 [background-image:linear-gradient(rgba(79,209,197,0.08)_1px,transparent_1px),linear-gradient(90deg,rgba(79,209,197,0.08)_1px,transparent_1px)] [background-size:34px_34px]" />
      <section className="relative w-full max-w-xl overflow-hidden rounded-[34px] border border-[color:var(--color-border)] bg-[color:var(--color-surface-alpha)] shadow-[var(--shadow-modal)] backdrop-blur-xl">
        <div className="p-8 sm:p-10">
          <div className="mb-8">
            <div className="mb-6 inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-[image:var(--gradient-primary)] text-lg font-black text-white shadow-[0_16px_35px_rgba(79,209,197,0.3)]">
              G
            </div>
            <p className="text-xs font-black uppercase tracking-[0.28em] text-[color:var(--color-primary)]">{env.appName}</p>
            <h1 className="mt-4 text-3xl font-black tracking-tight text-[color:var(--color-text)]">관리자 로그인</h1>
          </div>
          {sessionExpired ? (
            <div className="mb-6 rounded-[20px] border border-amber-200 bg-[linear-gradient(180deg,#fffbeb_0%,#fff7d6_100%)] px-4 py-3 text-sm font-medium text-amber-800 shadow-sm">
              로그인 상태가 만료되었습니다. 다시 로그인해 주세요.
            </div>
          ) : null}
          <LoginForm />
        </div>
      </section>
    </main>
  );
}
