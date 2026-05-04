import axios from 'axios';
import type { AxiosRequestConfig } from 'axios';
import { env } from '@/shared/config/env';
import { authStorage } from '@/features/auth/model/authStorage';

export type ApiRequestConfig = AxiosRequestConfig & {
  skipAuthRedirect?: boolean;
};

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

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const requestUrl = String(error.config?.url ?? '');
    const isLoginRequest = requestUrl.includes('/api/v1/auth/login');
    const isLogoutRequest = requestUrl.includes('/api/v1/auth/logout');
    const skipAuthRedirect = Boolean((error.config as ApiRequestConfig | undefined)?.skipAuthRedirect);

    if (status === 401 && !isLoginRequest && !isLogoutRequest && !skipAuthRedirect) {
      authStorage.clear();
      const nextUrl = `${window.location.origin}/login?reason=session-expired`;

      if (window.location.href !== nextUrl) {
        window.location.assign(nextUrl);
      }
    }

    return Promise.reject(error);
  }
);
