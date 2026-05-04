import type { Session } from '@/entities/session/model/types';

const ACCESS_TOKEN_KEY = 'guardrail.backoffice.accessToken';
const REFRESH_TOKEN_KEY = 'guardrail.backoffice.refreshToken';
const SESSION_ID_KEY = 'guardrail.backoffice.sessionId';
const USER_ID_KEY = 'guardrail.backoffice.userId';
const ROLE_KEY = 'guardrail.backoffice.role';
const TEMPORARY_PASSWORD_KEY = 'guardrail.backoffice.temporaryPassword';
const TEMP_PASSWORD_PROMPT_DISMISSED_KEY = 'guardrail.backoffice.temporaryPasswordPromptDismissed';

export const authStorage = {
  getAccessToken() {
    return window.localStorage.getItem(ACCESS_TOKEN_KEY);
  },
  getRefreshToken() {
    return window.localStorage.getItem(REFRESH_TOKEN_KEY);
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
  isTemporaryPassword() {
    return window.localStorage.getItem(TEMPORARY_PASSWORD_KEY) === 'true';
  },
  isTemporaryPasswordPromptDismissed() {
    return window.sessionStorage.getItem(TEMP_PASSWORD_PROMPT_DISMISSED_KEY) === 'true';
  },
  dismissTemporaryPasswordPrompt() {
    window.sessionStorage.setItem(TEMP_PASSWORD_PROMPT_DISMISSED_KEY, 'true');
  },
  setSession(session: Session) {
    window.localStorage.setItem(ACCESS_TOKEN_KEY, session.accessToken);
    window.localStorage.setItem(REFRESH_TOKEN_KEY, session.refreshToken);
    window.localStorage.setItem(SESSION_ID_KEY, session.sessionId);
    window.localStorage.setItem(USER_ID_KEY, session.userId);
    window.localStorage.setItem(ROLE_KEY, session.role);
    window.localStorage.setItem(TEMPORARY_PASSWORD_KEY, String(session.temporaryPassword));
    window.sessionStorage.removeItem(TEMP_PASSWORD_PROMPT_DISMISSED_KEY);
  },
  clear() {
    window.localStorage.removeItem(ACCESS_TOKEN_KEY);
    window.localStorage.removeItem(REFRESH_TOKEN_KEY);
    window.localStorage.removeItem(SESSION_ID_KEY);
    window.localStorage.removeItem(USER_ID_KEY);
    window.localStorage.removeItem(ROLE_KEY);
    window.localStorage.removeItem(TEMPORARY_PASSWORD_KEY);
    window.sessionStorage.removeItem(TEMP_PASSWORD_PROMPT_DISMISSED_KEY);
  }
};
