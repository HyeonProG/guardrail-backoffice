import { Link } from 'react-router-dom';
import { authStorage } from '@/features/auth/model/authStorage';
import { env } from '@/shared/config/env';

export function UnauthorizedPage() {
  const hasToken = Boolean(authStorage.getAccessToken());

  return (
    <main className="flex min-h-screen items-center justify-center bg-[radial-gradient(circle_at_top_left,_rgba(14,165,233,0.14),_transparent_30%),radial-gradient(circle_at_bottom_right,_rgba(245,158,11,0.14),_transparent_28%),linear-gradient(180deg,#f8fafc_0%,#edf4f7_100%)] px-4 py-10">
      <section className="w-full max-w-[560px] rounded-[32px] border border-white/80 bg-[linear-gradient(180deg,rgba(255,255,255,0.98)_0%,rgba(248,250,252,0.94)_100%)] p-10 text-center shadow-[0_38px_90px_rgba(15,23,42,0.14)]">
        <p className="text-xs font-semibold uppercase tracking-[0.28em] text-sky-700">{env.appName}</p>
        <h1 className="mt-3 text-3xl font-semibold text-slate-950">접근 권한이 없습니다</h1>
        <p className="mt-4 text-sm leading-7 text-slate-600">
          이 화면은 현재 로그인한 계정으로 열 수 없습니다. 홈 화면으로 돌아가거나, 필요한 권한이 있는 계정으로 다시 로그인해 주세요.
        </p>
        <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
          <Link
            className="inline-flex h-11 items-center justify-center rounded-2xl border border-sky-900/90 bg-[linear-gradient(135deg,#0f172a_0%,#1e3a8a_100%)] px-5 text-sm font-semibold text-white shadow-lg hover:-translate-y-0.5"
            to={hasToken ? '/dashboard' : '/login'}
          >
            {hasToken ? '홈 화면으로 가기' : '로그인하러 가기'}
          </Link>
          {hasToken ? (
            <Link
              className="inline-flex h-11 items-center justify-center rounded-2xl border border-slate-200 bg-white/90 px-5 text-sm font-semibold text-slate-700 shadow-sm hover:-translate-y-0.5 hover:bg-slate-50"
              to="/login"
              onClick={() => authStorage.clear()}
            >
              다른 계정으로 로그인
            </Link>
          ) : null}
        </div>
      </section>
    </main>
  );
}
