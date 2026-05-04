import type { Session } from '@/entities/session/model/types';

const ACCESS_TOKEN_KEY = 'guardrail.backoffice.accessToken';
const REFRESH_TOKEN_KEY = 'guardrail.backoffice.refreshToken';
const SESSION_ID_KEY = 'guardrail.backoffice.sessionId';
const USER_ID_KEY = 'guardrail.backoffice.userId';
const ROLE_KEY = 'guardrail.backoffice.role';
const ACCESS_TOKEN_EXPIRED_AT_KEY = 'guardrail.backoffice.accessTokenExpiredAt';
const REFRESH_TOKEN_EXPIRED_AT_KEY = 'guardrail.backoffice.refreshTokenExpiredAt';
const TEMPORARY_PASSWORD_KEY = 'guardrail.backoffice.temporaryPassword';
const TEMP_PASSWORD_PROMPT_DISMISSED_KEY = 'guardrail.backoffice.temporaryPasswordPromptDismissed';

type JwtPayload = {
  role?: string;
  sessionId?: string;
  accessTokenId?: string;
};

const normalizeJwtToken = (value: string | null) => {
  if (!value) {
    return null;
  }

  const trimmed = value.trim().replace(/^"|"$/g, '');
  const segments = trimmed.split('.');

  if (segments.length !== 3 || segments.some((segment) => !segment)) {
    return null;
  }

  return trimmed;
};

const parseJwtPayload = (token: string | null): JwtPayload | null => {
  const normalized = normalizeJwtToken(token);

  if (!normalized) {
    return null;
  }

  try {
    const payload = normalized.split('.')[1];
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const decoded = window.atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '='));
    return JSON.parse(decoded) as JwtPayload;
  } catch {
    return null;
  }
};

const isLikelyAccessToken = (token: string | null) => {
  const payload = parseJwtPayload(token);
  return Boolean(payload?.role && payload?.accessTokenId && payload?.sessionId);
};

const isLikelyRefreshToken = (token: string | null) => {
  const payload = parseJwtPayload(token);
  return Boolean(payload?.sessionId && !payload?.role && !payload?.accessTokenId);
};

const getCanonicalTokens = () => {
  const storedAccessToken = normalizeJwtToken(window.localStorage.getItem(ACCESS_TOKEN_KEY));
  const storedRefreshToken = normalizeJwtToken(window.localStorage.getItem(REFRESH_TOKEN_KEY));

  const accessToken =
    isLikelyAccessToken(storedAccessToken)
      ? storedAccessToken
      : isLikelyAccessToken(storedRefreshToken)
        ? storedRefreshToken
        : storedAccessToken;

  const refreshToken =
    isLikelyRefreshToken(storedRefreshToken)
      ? storedRefreshToken
      : isLikelyRefreshToken(storedAccessToken)
        ? storedAccessToken
        : storedRefreshToken;

  if (accessToken && accessToken !== storedAccessToken) {
    window.localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  }

  if (refreshToken && refreshToken !== storedRefreshToken) {
    window.localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }

  return { accessToken, refreshToken };
};

export const authStorage = {
  getAccessToken() {
    return getCanonicalTokens().accessToken;
  },
  getRefreshToken() {
    return getCanonicalTokens().refreshToken;
  },
  getSessionId() {
    return window.localStorage.getItem(SESSION_ID_KEY);
  },
  getActorId() {
    return window.localStorage.getItem(USER_ID_KEY);
  },
  getRole() {
    return window.localStorage.getItem(ROLE_KEY);
  },
  getAccessTokenExpiredAt() {
    return window.localStorage.getItem(ACCESS_TOKEN_EXPIRED_AT_KEY);
  },
  getRefreshTokenExpiredAt() {
    return window.localStorage.getItem(REFRESH_TOKEN_EXPIRED_AT_KEY);
  },
  isTemporaryPassword() {
    return window.localStorage.getItem(TEMPORARY_PASSWORD_KEY) === 'true';
  },
  isAccessTokenExpired() {
    const expiredAt = this.getAccessTokenExpiredAt();
    if (!expiredAt) {
      return false;
    }
    return new Date(expiredAt).getTime() <= Date.now();
  },
  isTemporaryPasswordPromptDismissed() {
    return window.sessionStorage.getItem(TEMP_PASSWORD_PROMPT_DISMISSED_KEY) === 'true';
  },
  dismissTemporaryPasswordPrompt() {
    window.sessionStorage.setItem(TEMP_PASSWORD_PROMPT_DISMISSED_KEY, 'true');
  },
  setSession(session: Session) {
    const accessToken =
      isLikelyAccessToken(session.accessToken) || !isLikelyAccessToken(session.refreshToken)
        ? session.accessToken
        : session.refreshToken;
    const refreshToken =
      isLikelyRefreshToken(session.refreshToken) || !isLikelyRefreshToken(session.accessToken)
        ? session.refreshToken
        : session.accessToken;

    window.localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    window.localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    window.localStorage.setItem(SESSION_ID_KEY, session.sessionId);
    window.localStorage.setItem(USER_ID_KEY, session.userId);
    window.localStorage.setItem(ROLE_KEY, session.role);
    window.localStorage.setItem(ACCESS_TOKEN_EXPIRED_AT_KEY, session.accessTokenExpiredAt);
    window.localStorage.setItem(REFRESH_TOKEN_EXPIRED_AT_KEY, session.refreshTokenExpiredAt);
    window.localStorage.setItem(TEMPORARY_PASSWORD_KEY, String(session.temporaryPassword));
    window.sessionStorage.removeItem(TEMP_PASSWORD_PROMPT_DISMISSED_KEY);
  },
  clear() {
    window.localStorage.removeItem(ACCESS_TOKEN_KEY);
    window.localStorage.removeItem(REFRESH_TOKEN_KEY);
    window.localStorage.removeItem(SESSION_ID_KEY);
    window.localStorage.removeItem(USER_ID_KEY);
    window.localStorage.removeItem(ROLE_KEY);
    window.localStorage.removeItem(ACCESS_TOKEN_EXPIRED_AT_KEY);
    window.localStorage.removeItem(REFRESH_TOKEN_EXPIRED_AT_KEY);
    window.localStorage.removeItem(TEMPORARY_PASSWORD_KEY);
    window.sessionStorage.removeItem(TEMP_PASSWORD_PROMPT_DISMISSED_KEY);
  }
};
