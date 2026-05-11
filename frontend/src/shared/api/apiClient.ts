import axios from 'axios';
import type { AxiosRequestConfig } from 'axios';
import { env } from '@/shared/config/env';
import { authStorage } from '@/features/auth/model/authStorage';
import type { ApiResponse } from '@/shared/api/types';

export type ApiRequestConfig = AxiosRequestConfig & {
  skipAuthRedirect?: boolean;
  retryAfterRefresh?: boolean;
};

type TokenRefreshResult = {
  accessToken: string;
  refreshToken: string;
  sessionId: string;
  accessTokenExpiredAt: string;
  refreshTokenExpiredAt: string;
};

let refreshPromise: Promise<string | null> | null = null;

export const apiClient = axios.create({
  baseURL: env.apiBaseUrl,
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 10000
});

apiClient.interceptors.request.use((config) => {
  const token = authStorage.getAccessToken();

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

async function refreshAccessToken() {
  if (refreshPromise) {
    return refreshPromise;
  }

  refreshPromise = requestTokenRefresh().finally(() => {
    refreshPromise = null;
  });

  return refreshPromise;
}

async function requestTokenRefresh() {
  const sessionId = authStorage.getSessionId();
  const refreshToken = authStorage.getRefreshToken();

  if (!sessionId || !refreshToken) {
    return null;
  }

  const response = await axios.post<ApiResponse<TokenRefreshResult>>(
    `${env.apiBaseUrl}/api/v1/auth/token/refresh`,
    { sessionId, refreshToken },
    {
      headers: {
        'Content-Type': 'application/json'
      },
      timeout: 10000
    }
  );
  const refreshed = response.data.result;

  authStorage.updateTokens({
    accessToken: refreshed.accessToken,
    refreshToken: refreshed.refreshToken,
    sessionId: refreshed.sessionId
  });

  return refreshed.accessToken;
}

function redirectToLogin() {
  authStorage.clear();
  const nextUrl = `${window.location.origin}/login?reason=session-expired`;

  if (window.location.href !== nextUrl) {
    window.location.assign(nextUrl);
  }
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status = error.response?.status;
    const requestUrl = String(error.config?.url ?? '');
    const isLoginRequest = requestUrl.includes('/api/v1/auth/login');
    const isLogoutRequest = requestUrl.includes('/api/v1/auth/logout');
    const isRefreshRequest = requestUrl.includes('/api/v1/auth/token/refresh');
    const skipAuthRedirect = Boolean((error.config as ApiRequestConfig | undefined)?.skipAuthRedirect);
    const originalRequest = error.config as ApiRequestConfig | undefined;

    if (
      status === 401 &&
      originalRequest &&
      !originalRequest.retryAfterRefresh &&
      !isLoginRequest &&
      !isLogoutRequest &&
      !isRefreshRequest &&
      !skipAuthRedirect
    ) {
      try {
        const accessToken = await refreshAccessToken();

        if (accessToken) {
          originalRequest.retryAfterRefresh = true;
          originalRequest.headers = {
            ...originalRequest.headers,
            Authorization: `Bearer ${accessToken}`
          };
          return apiClient(originalRequest);
        }
      } catch {
        redirectToLogin();
        return Promise.reject(error);
      }
    }

    if (status === 401 && !isLoginRequest && !isLogoutRequest && !skipAuthRedirect) {
      redirectToLogin();
    }

    return Promise.reject(error);
  }
);
