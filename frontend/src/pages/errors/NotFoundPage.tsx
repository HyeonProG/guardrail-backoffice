import { Link } from 'react-router-dom';
import { authStorage } from '@/features/auth/model/authStorage';
import { env } from '@/shared/config/env';

export function NotFoundPage() {
  const hasToken = Boolean(authStorage.getAccessToken());

  return (
    <main className="flex min-h-screen items-center justify-center bg-surface px-4 py-10">
      <section className="w-full max-w-[520px] rounded-[28px] border border-slate-200/80 bg-white p-10 text-center shadow-sm">
        <p className="text-sm font-semibold text-slate-500">{env.appName}</p>
        <h1 className="mt-3 text-3xl font-semibold text-slate-950">페이지를 찾을 수 없습니다</h1>
        <p className="mt-4 text-sm leading-7 text-slate-600">
          주소가 잘못되었거나, 이미 이동된 화면입니다. 홈 화면으로 돌아가서 다시 이동해 주세요.
        </p>
        <div className="mt-8 flex items-center justify-center gap-3">
          <Link
            className="inline-flex h-11 items-center justify-center rounded-full bg-slate-950 px-5 text-sm font-semibold text-white hover:bg-slate-800"
            to={hasToken ? '/dashboard' : '/login'}
          >
            {hasToken ? '홈 화면으로 가기' : '로그인하러 가기'}
          </Link>
        </div>
      </section>
    </main>
  );
}
