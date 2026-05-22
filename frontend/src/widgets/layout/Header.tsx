import { Link, useLocation } from 'react-router-dom';
import { useEffect, useMemo, useRef, useState } from 'react';
import type { SVGProps } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTheme } from '@/app/providers/ThemeProvider';
import { Button } from '@/shared/ui/Button';
import type { NavigationGroup } from '@/widgets/layout/model/navigation';
import { authStorage } from '@/features/auth/model/authStorage';
import { getUser } from '@/entities/user/api/userApi';
import type { UserRole } from '@/entities/user/model/types';

type HeaderProps = {
  groups: NavigationGroup[];
  sidebarCollapsed: boolean;
  onLogout: () => void;
  onToggleSidebar: () => void;
};

type SidebarIconProps = SVGProps<SVGSVGElement>;

const sidebarIconProps = {
  fill: 'none',
  stroke: 'currentColor',
  strokeLinecap: 'round',
  strokeLinejoin: 'round',
  strokeWidth: 2,
  viewBox: '0 0 24 24'
} as const;

const HomeIcon = (props: SidebarIconProps) => (
  <svg {...sidebarIconProps} {...props}>
    <path d="m3 11 9-8 9 8" />
    <path d="M5 10v10h14V10" />
    <path d="M9 20v-6h6v6" />
  </svg>
);

const BoxIcon = (props: SidebarIconProps) => (
  <svg {...sidebarIconProps} {...props}>
    <path d="m21 8-9-5-9 5 9 5 9-5Z" />
    <path d="M3 8v8l9 5 9-5V8" />
    <path d="M12 13v8" />
  </svg>
);

const CheckCircleIcon = (props: SidebarIconProps) => (
  <svg {...sidebarIconProps} {...props}>
    <circle cx="12" cy="12" r="9" />
    <path d="m8.5 12.5 2.2 2.2 4.8-5.2" />
  </svg>
);

const ClockAlertIcon = (props: SidebarIconProps) => (
  <svg {...sidebarIconProps} {...props}>
    <circle cx="12" cy="12" r="9" />
    <path d="M12 7v5l3 2" />
    <path d="M12 17h.01" />
  </svg>
);

const ListTreeIcon = (props: SidebarIconProps) => (
  <svg {...sidebarIconProps} {...props}>
    <path d="M4 6h6" />
    <path d="M4 12h10" />
    <path d="M4 18h14" />
    <path d="M18 8v8" />
  </svg>
);

const SlidersIcon = (props: SidebarIconProps) => (
  <svg {...sidebarIconProps} {...props}>
    <path d="M4 6h8" />
    <path d="M16 6h4" />
    <path d="M4 12h4" />
    <path d="M12 12h8" />
    <path d="M4 18h11" />
    <path d="M19 18h1" />
    <circle cx="14" cy="6" r="2" />
    <circle cx="10" cy="12" r="2" />
    <circle cx="17" cy="18" r="2" />
  </svg>
);

const UsersIcon = (props: SidebarIconProps) => (
  <svg {...sidebarIconProps} {...props}>
    <path d="M16 21v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2" />
    <circle cx="9.5" cy="7" r="4" />
    <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
    <path d="M16 3.13a4 4 0 0 1 0 7.75" />
  </svg>
);

const UserIcon = (props: SidebarIconProps) => (
  <svg {...sidebarIconProps} {...props}>
    <circle cx="12" cy="8" r="4" />
    <path d="M4 21a8 8 0 0 1 16 0" />
  </svg>
);

const itemIconMap: Record<string, (props: SidebarIconProps) => JSX.Element> = {
  메인: HomeIcon,
  '상품 관리': BoxIcon,
  '승인 완료 상품': CheckCircleIcon,
  '승인 요청 관리': ClockAlertIcon,
  '카테고리 관리': ListTreeIcon,
  '상품 옵션 관리': SlidersIcon,
  '사용자 관리': UsersIcon,
  '내 정보': UserIcon
};

export function Header({ groups, sidebarCollapsed, onLogout, onToggleSidebar }: HeaderProps) {
  const location = useLocation();
  const { theme, toggleTheme } = useTheme();
  const [profileOpen, setProfileOpen] = useState(false);
  const [sidebarHovered, setSidebarHovered] = useState(false);
  const profileRef = useRef<HTMLDivElement | null>(null);
  const userId = authStorage.getActorId() ?? '';
  const currentRole = (authStorage.getRole() as UserRole | null) ?? null;
  const sidebarExpanded = !sidebarCollapsed || sidebarHovered;

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

      if (profileRef.current && !profileRef.current.contains(target)) {
        setProfileOpen(false);
      }
    };

    window.addEventListener('mousedown', handleOutsideClick);
    return () => window.removeEventListener('mousedown', handleOutsideClick);
  }, []);

  useEffect(() => {
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
    <>
      <aside
        className={`group/sidebar fixed inset-y-0 left-0 z-40 hidden border-r border-[color:var(--color-border)] bg-[color:var(--color-sidebar)] p-4 text-[color:var(--color-text)] shadow-[18px_0_55px_rgba(23,37,84,0.08)] backdrop-blur-xl transition-[width,box-shadow] duration-300 lg:flex lg:flex-col ${
          sidebarExpanded ? 'w-[272px]' : 'w-[88px]'
        }`}
        onMouseEnter={() => setSidebarHovered(true)}
        onMouseLeave={() => setSidebarHovered(false)}
      >
        <div className={`flex items-center gap-3 py-5 ${sidebarExpanded ? 'px-4' : 'justify-center px-0'}`}>
          <Link className={`flex min-w-0 items-center gap-3 ${sidebarExpanded ? 'flex-1' : 'justify-center'}`} to="/dashboard">
            <span className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-[18px] bg-[image:var(--gradient-primary)] text-base font-black text-white shadow-[0_14px_28px_rgba(79,209,197,0.24)]">
              G
            </span>
            <span className={`min-w-0 ${sidebarExpanded ? 'block' : 'hidden'}`}>
              <span className="block truncate text-lg font-black leading-tight">Guardrail</span>
            </span>
          </Link>
        </div>

        <nav className={`flex-1 overflow-y-auto py-4 ${sidebarExpanded ? 'px-4' : 'px-0'}`}>
          <div className="space-y-7">
            {visibleGroups.map((group) => (
              <section key={group.label}>
                <p
                  className={`px-3 text-xs font-bold uppercase tracking-[0.18em] text-[color:var(--color-muted)] ${
                    sidebarExpanded ? '' : 'sr-only'
                  }`}
                >
                  {group.label}
                </p>
                <div className="mt-3 space-y-1">
                  {group.items.map((item) => {
                    const active = isPathActive(item.path);
                    const Icon = itemIconMap[item.label] ?? BoxIcon;

                    return (
                      <Link
                        key={item.path}
                        className={`group flex items-center gap-3 rounded-2xl text-sm font-bold transition duration-200 ${
                          sidebarExpanded ? 'px-3 py-3' : 'mx-auto h-14 w-14 justify-center p-0'
                        } ${
                          active
                            ? 'bg-[color:var(--color-primary-soft)] text-[color:var(--color-primary-deep)] shadow-[0_14px_30px_rgba(79,209,197,0.2)] ring-1 ring-[color:var(--color-primary)]/30'
                            : 'text-[color:var(--color-muted)] hover:translate-x-0.5 hover:bg-[color:var(--color-sidebar-soft)] hover:text-[color:var(--color-primary-deep)]'
                        }`}
                        to={item.path}
                        title={sidebarExpanded ? undefined : item.label}
                      >
                        <span
                          className={`inline-flex shrink-0 items-center justify-center rounded-xl transition ${
                            sidebarExpanded ? 'h-8 w-8' : 'h-11 w-11'
                          } ${
                            active
                              ? 'bg-[color:var(--color-primary)] text-white'
                              : 'bg-[color:var(--color-surface-muted)] text-[color:var(--color-muted)] group-hover:bg-[color:var(--color-primary-soft)]'
                          }`}
                        >
                          <Icon className={sidebarExpanded ? 'h-4 w-4' : 'h-5 w-5'} aria-hidden="true" />
                        </span>
                        <span className={`min-w-0 truncate ${sidebarExpanded ? 'block' : 'hidden'}`}>{item.label}</span>
                      </Link>
                    );
                  })}
                </div>
              </section>
            ))}
          </div>
        </nav>

        <div
          className={`grid grid-cols-2 gap-2 border-t border-[color:var(--color-border)] px-2 py-5 transition duration-200 ${
            sidebarExpanded ? 'opacity-100' : 'pointer-events-none opacity-0'
          }`}
        >
          <button
            className="flex h-11 items-center justify-center rounded-2xl border border-[color:var(--color-border)] bg-[color:var(--color-surface)] text-sm font-black text-[color:var(--color-muted)] shadow-sm transition hover:-translate-y-0.5 hover:bg-[color:var(--color-primary-soft)] hover:text-[color:var(--color-primary-deep)]"
            type="button"
            onClick={toggleTheme}
            aria-label={theme === 'dark' ? '라이트 모드로 변경' : '다크 모드로 변경'}
          >
            <span>{theme === 'dark' ? '☀' : '☾'}</span>
          </button>
          <button
            className="flex h-11 items-center justify-center rounded-2xl border border-[color:var(--color-border)] bg-[color:var(--color-surface)] text-xs font-black text-[color:var(--color-muted)] shadow-sm transition hover:-translate-y-0.5 hover:bg-[color:var(--color-primary-soft)] hover:text-[color:var(--color-primary-deep)]"
            type="button"
            onClick={onToggleSidebar}
            aria-label={sidebarCollapsed ? '사이드바 펼치기' : '사이드바 접기'}
          >
            {sidebarCollapsed ? '>>' : '<<'}
          </button>
        </div>
      </aside>

      <header
        className={`sticky top-0 z-30 border-b border-[color:var(--color-border)] bg-[color:var(--color-surface-alpha)] backdrop-blur-xl transition-[margin] duration-300 ${
          sidebarCollapsed ? 'lg:ml-[88px]' : 'lg:ml-[272px]'
        }`}
      >
        <div className="flex h-[76px] items-center justify-between gap-4 px-5 sm:px-6 lg:px-8">
          <div className="flex min-w-0 flex-1 items-center gap-4">
            <Link
              className="inline-flex h-11 w-11 items-center justify-center rounded-2xl border border-slate-200 bg-white text-lg font-bold text-blue-700 shadow-sm lg:hidden"
              to="/dashboard"
            >
              G
            </Link>
          </div>

          <div className="flex items-center gap-3">
            <div className="relative hidden md:block" ref={profileRef}>
              <button
                className="inline-flex items-center gap-3 rounded-2xl border border-[color:var(--color-border)] bg-[color:var(--color-surface)] px-3 py-2 text-left shadow-sm transition duration-200 hover:-translate-y-0.5 hover:border-[color:var(--color-primary)] hover:shadow-md"
                onClick={() => setProfileOpen((current) => !current)}
                type="button"
              >
                <span className="inline-flex h-10 w-10 items-center justify-center rounded-full bg-[image:var(--gradient-primary)] text-sm font-black text-white">
                  {(userQuery.data?.name ?? 'U').slice(0, 1)}
                </span>
                <span className="min-w-0">
                  <span className="block truncate text-sm font-bold text-[color:var(--color-text)]">{profileLabel}</span>
                  <span className="block truncate text-xs text-[color:var(--color-muted)]">
                    {userQuery.data?.email ?? authStorage.getRole() ?? '사용자 정보 조회 중'}
                  </span>
                </span>
                <span className="text-xs text-slate-400">⌄</span>
              </button>

              <div
                className={`absolute right-0 top-full mt-3 w-72 rounded-[22px] border border-[color:var(--color-border)] bg-[color:var(--color-surface)] p-4 shadow-[var(--shadow-card)] transition duration-300 ${
                  profileOpen ? 'pointer-events-auto translate-y-0 opacity-100' : 'pointer-events-none translate-y-2 opacity-0'
                }`}
              >
                <div className="border-b border-[color:var(--color-border)] pb-3">
                  <p className="text-sm font-bold text-[color:var(--color-text)]">{userQuery.data?.name ?? '내 정보'}</p>
                  <p className="mt-1 text-xs text-[color:var(--color-muted)]">{userQuery.data?.email ?? '-'}</p>
                  <p className="mt-2 text-xs font-semibold text-[color:var(--color-primary-deep)]">
                    역할 {userQuery.data?.role ?? authStorage.getRole() ?? '-'}
                  </p>
                </div>
                <div className="mt-3 space-y-2">
                  <Link
                    className="block rounded-xl px-3 py-2 text-sm font-bold text-slate-700 transition hover:bg-blue-50 hover:text-blue-700"
                    to="/my-account"
                  >
                    내 정보 수정
                  </Link>
                </div>
              </div>
            </div>
            <Button type="button" variant="secondary" onClick={onLogout}>
              로그아웃
            </Button>
          </div>
        </div>

        <div className="border-t border-[color:var(--color-border)] px-5 py-3 lg:hidden sm:px-6">
          <div className="flex gap-2 overflow-x-auto pb-1">
            {visibleGroups.flatMap((group) => group.items).map((item) => (
              <Link
                key={item.path}
                className={`whitespace-nowrap rounded-full px-4 py-2 text-sm font-bold transition ${
                  isPathActive(item.path)
                    ? 'bg-[color:var(--color-primary)] text-[#172554] shadow-md'
                    : 'bg-[color:var(--color-surface)] text-[color:var(--color-muted)] ring-1 ring-[color:var(--color-border)] hover:text-[color:var(--color-primary-deep)]'
                }`}
                to={item.path}
              >
                {item.label}
              </Link>
            ))}
          </div>
        </div>
      </header>
    </>
  );
}
