import { Link, useLocation } from 'react-router-dom';
import { useEffect, useMemo, useRef, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Button } from '@/shared/ui/Button';
import { env } from '@/shared/config/env';
import type { NavigationGroup } from '@/widgets/layout/model/navigation';
import { authStorage } from '@/features/auth/model/authStorage';
import { getUser } from '@/entities/user/api/userApi';
import type { UserRole } from '@/entities/user/model/types';

type HeaderProps = {
  groups: NavigationGroup[];
  onLogout: () => void;
};

export function Header({ groups, onLogout }: HeaderProps) {
  const location = useLocation();
  const [openGroup, setOpenGroup] = useState<string | null>(null);
  const [profileOpen, setProfileOpen] = useState(false);
  const navRef = useRef<HTMLDivElement | null>(null);
  const profileRef = useRef<HTMLDivElement | null>(null);
  const userId = authStorage.getActorId() ?? '';
  const currentRole = (authStorage.getRole() as UserRole | null) ?? null;

  const userQuery = useQuery({
    queryKey: ['header-user', userId],
    queryFn: () => getUser(userId, { skipAuthRedirect: true }),
    enabled: Boolean(userId)
  });

  const profileLabel = useMemo(() => {
    if (userQuery.data?.name) {
      return userQuery.data.name;
    }
    return authStorage.getRole() ?? '내 정보';
  }, [userQuery.data?.name]);

  const visibleGroups = useMemo(
    () =>
      groups
        .map((group) => ({
          ...group,
          items: group.items.filter(
            (item) => !item.allowedRoles || (currentRole ? item.allowedRoles.includes(currentRole) : false)
          )
        }))
        .filter((group) => group.items.length > 0),
    [currentRole, groups]
  );

  useEffect(() => {
    const handleOutsideClick = (event: MouseEvent) => {
      const target = event.target as Node;

      if (navRef.current && !navRef.current.contains(target)) {
        setOpenGroup(null);
      }

      if (profileRef.current && !profileRef.current.contains(target)) {
        setProfileOpen(false);
      }
    };

    window.addEventListener('mousedown', handleOutsideClick);
    return () => window.removeEventListener('mousedown', handleOutsideClick);
  }, []);

  useEffect(() => {
    setOpenGroup(null);
    setProfileOpen(false);
  }, [location.pathname]);

  const isPathActive = (path: string) => {
    if (path === '/products') {
      return (
        location.pathname === '/products' ||
        location.pathname === '/products/new' ||
        (/^\/products\/[^/]+(\/edit)?$/.test(location.pathname) &&
          !location.pathname.startsWith('/products/approved'))
      );
    }

    if (path === '/products/approved') {
      return location.pathname === '/products/approved';
    }

    return location.pathname === path || location.pathname.startsWith(`${path}/`);
  };

  return (
    <header className="sticky top-0 z-40 border-b border-white/60 bg-white/70 backdrop-blur-2xl">
      <div className="mx-auto flex max-w-[1480px] items-center justify-between gap-6 px-5 py-4 sm:px-6 lg:px-8">
        <div className="flex min-w-0 items-center gap-8">
          <Link
            className="group inline-flex items-center gap-3 rounded-[28px] border border-white/80 bg-[linear-gradient(135deg,rgba(255,255,255,0.96)_0%,rgba(241,245,249,0.94)_100%)] px-4 py-3 shadow-[0_16px_40px_rgba(15,23,42,0.08)] transition duration-300 hover:-translate-y-0.5 hover:shadow-[0_20px_48px_rgba(15,23,42,0.12)]"
            to="/dashboard"
          >
            <span className="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#0f172a_0%,#1d4ed8_100%)] text-sm font-semibold text-white shadow-lg transition duration-300 group-hover:-rotate-3">
              G
            </span>
            <div className="min-w-0">
              <p className="text-sm font-semibold leading-none text-slate-950">{env.appName}</p>
              <p className="mt-1 text-xs uppercase tracking-[0.18em] text-slate-500">Backoffice Control Center</p>
            </div>
          </Link>

          <nav className="hidden items-center gap-2 lg:flex" ref={navRef}>
            {visibleGroups.map((group) => (
              <div key={group.label} className="relative">
                <button
                  className={`inline-flex items-center gap-2 rounded-full px-4 py-2 text-sm font-medium transition duration-200 ${
                    openGroup === group.label
                      ? 'bg-[linear-gradient(135deg,#0f172a_0%,#1e3a8a_100%)] text-white shadow-lg'
                      : 'text-slate-700 hover:bg-white hover:text-slate-950 hover:shadow-sm'
                  }`}
                  onClick={() => setOpenGroup((current) => (current === group.label ? null : group.label))}
                  type="button"
                >
                  {group.label}
                  <span
                    className={`text-xs transition ${openGroup === group.label ? 'rotate-180 text-slate-300' : 'text-slate-400'}`}
                  >
                    ▼
                  </span>
                </button>
                <div
                  className={`absolute left-0 top-full mt-3 w-80 rounded-[24px] border border-white/80 bg-[linear-gradient(180deg,rgba(255,255,255,0.98)_0%,rgba(248,250,252,0.96)_100%)] p-3 shadow-2xl shadow-slate-900/10 transition duration-300 ${
                    openGroup === group.label
                      ? 'pointer-events-auto translate-y-0 opacity-100'
                      : 'pointer-events-none translate-y-2 opacity-0'
                  }`}
                >
                  <div className="space-y-2">
                    {group.items.map((item) => (
                      <Link
                        key={item.path}
                        className={`block rounded-[20px] px-4 py-3 transition duration-200 ${
                          isPathActive(item.path)
                            ? 'bg-[linear-gradient(135deg,#0f172a_0%,#1e3a8a_100%)] text-white shadow-lg'
                            : 'bg-white/80 text-slate-700 hover:bg-slate-100 hover:text-slate-950'
                        }`}
                        to={item.path}
                      >
                        <p className="text-sm font-semibold">{item.label}</p>
                        <p
                          className={`mt-1 text-xs ${
                            isPathActive(item.path) ? 'text-slate-300' : 'text-slate-500'
                          }`}
                        >
                          {item.description}
                        </p>
                      </Link>
                    ))}
                  </div>
                </div>
              </div>
            ))}
          </nav>
        </div>

        <div className="flex items-center gap-3">
          <div className="relative hidden md:block" ref={profileRef}>
            <button
              className="inline-flex items-center gap-3 rounded-[28px] border border-white/80 bg-[linear-gradient(135deg,rgba(255,255,255,0.98)_0%,rgba(241,245,249,0.94)_100%)] px-4 py-2 text-left shadow-[0_16px_40px_rgba(15,23,42,0.08)] transition duration-200 hover:border-slate-300 hover:shadow-[0_20px_48px_rgba(15,23,42,0.12)]"
              onClick={() => setProfileOpen((current) => !current)}
              type="button"
            >
              <span className="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#020617_0%,#0f172a_100%)] text-sm font-semibold text-white">
                {(userQuery.data?.name ?? 'U').slice(0, 1)}
              </span>
              <span className="min-w-0">
                <span className="block truncate text-sm font-semibold text-slate-950">{profileLabel}</span>
                <span className="block truncate text-xs text-slate-500">
                  {userQuery.data?.email ?? authStorage.getRole() ?? '사용자 정보 조회 중'}
                </span>
              </span>
            </button>

            <div
              className={`absolute right-0 top-full mt-3 w-72 rounded-[24px] border border-white/80 bg-[linear-gradient(180deg,rgba(255,255,255,0.98)_0%,rgba(248,250,252,0.96)_100%)] p-4 shadow-2xl shadow-slate-900/10 transition duration-300 ${
                profileOpen ? 'pointer-events-auto translate-y-0 opacity-100' : 'pointer-events-none translate-y-2 opacity-0'
              }`}
            >
              <div className="border-b border-slate-100 pb-3">
                <p className="text-sm font-semibold text-slate-950">{userQuery.data?.name ?? '내 정보'}</p>
                <p className="mt-1 text-xs text-slate-500">{userQuery.data?.email ?? '-'}</p>
                <p className="mt-2 text-xs font-medium text-slate-400">역할 {userQuery.data?.role ?? authStorage.getRole() ?? '-'}</p>
              </div>
              <div className="mt-3 space-y-2">
                <Link
                  className="block rounded-xl px-3 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-100 hover:text-slate-950"
                  to="/my-account"
                >
                  내 정보 수정
                </Link>
              </div>
            </div>
          </div>
          <Button type="button" variant="secondary" className="bg-white/85" onClick={onLogout}>
            로그아웃
          </Button>
        </div>
      </div>

      <div className="border-t border-slate-100/80 px-5 py-3 lg:hidden sm:px-6">
        <div className="flex gap-2 overflow-x-auto pb-1">
          {visibleGroups.flatMap((group) => group.items).map((item) => (
            <Link
              key={item.path}
              className={`whitespace-nowrap rounded-full px-4 py-2 text-sm font-medium transition ${
                isPathActive(item.path)
                  ? 'bg-[linear-gradient(135deg,#0f172a_0%,#1e3a8a_100%)] text-white shadow-md'
                  : 'bg-white/90 text-slate-600 ring-1 ring-slate-200 hover:text-slate-950'
              }`}
              to={item.path}
            >
              {item.label}
            </Link>
          ))}
        </div>
      </div>
    </header>
  );
}
