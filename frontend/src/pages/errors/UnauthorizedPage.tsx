import { Link } from 'react-router-dom';
import { authStorage } from '@/features/auth/model/authStorage';
import { env } from '@/shared/config/env';

export function UnauthorizedPage() {
  const hasToken = Boolean(authStorage.getAccessToken());

  return (
    <main className="flex min-h-screen items-center justify-center bg-surface px-4 py-10">
      <section className="w-full max-w-[520px] rounded-[28px] border border-slate-200/80 bg-white p-10 text-center shadow-sm">
        <p className="text-sm font-semibold text-slate-500">{env.appName}</p>
        <h1 className="mt-3 text-3xl font-semibold text-slate-950">접근 권한이 없습니다</h1>
        <p className="mt-4 text-sm leading-7 text-slate-600">
          이 화면은 현재 로그인한 계정으로 열 수 없습니다. 홈 화면으로 돌아가거나, 필요한 권한이 있는 계정으로 다시 로그인해 주세요.
        </p>
        <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
          <Link
            className="inline-flex h-11 items-center justify-center rounded-full bg-slate-950 px-5 text-sm font-semibold text-white hover:bg-slate-800"
            to={hasToken ? '/dashboard' : '/login'}
          >
            {hasToken ? '홈 화면으로 가기' : '로그인하러 가기'}
          </Link>
          {hasToken ? (
            <Link
              className="inline-flex h-11 items-center justify-center rounded-full border border-slate-200 px-5 text-sm font-semibold text-slate-700 hover:bg-slate-50"
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
