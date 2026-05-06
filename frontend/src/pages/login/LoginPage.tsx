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
    <main className="relative flex min-h-screen items-center justify-center overflow-hidden bg-[radial-gradient(circle_at_top_left,_rgba(14,165,233,0.14),_transparent_30%),radial-gradient(circle_at_bottom_right,_rgba(245,158,11,0.14),_transparent_28%),linear-gradient(180deg,#f8fafc_0%,#edf4f7_100%)] px-4 py-10">
      <div className="absolute inset-0 opacity-60 [background-image:linear-gradient(rgba(148,163,184,0.09)_1px,transparent_1px),linear-gradient(90deg,rgba(148,163,184,0.09)_1px,transparent_1px)] [background-size:32px_32px]" />
      <section className="relative w-full max-w-[440px] overflow-hidden rounded-[32px] border border-white/80 bg-[linear-gradient(180deg,rgba(255,255,255,0.98)_0%,rgba(248,250,252,0.94)_100%)] p-9 shadow-[0_38px_90px_rgba(15,23,42,0.14)]">
        <div className="mb-8">
          <p className="text-xs font-semibold uppercase tracking-[0.28em] text-sky-700">{env.appName}</p>
          <h1 className="mt-4 text-3xl font-semibold tracking-tight text-ink">관리자 로그인</h1>
          <p className="mt-3 text-sm leading-6 text-slate-500">상품 등록, 승인 요청, 기준 정보 관리를 위한 운영 콘솔에 접속합니다.</p>
        </div>
        {sessionExpired ? (
          <div className="mb-6 rounded-[20px] border border-amber-200 bg-[linear-gradient(180deg,#fffbeb_0%,#fff7d6_100%)] px-4 py-3 text-sm font-medium text-amber-800 shadow-sm">
            로그인 상태가 만료되었습니다. 다시 로그인해 주세요.
          </div>
        ) : null}
        <LoginForm />
      </section>
    </main>
  );
}
