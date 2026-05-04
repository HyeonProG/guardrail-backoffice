import { apiClient } from '@/shared/api/apiClient';
import type { Session } from '@/entities/session/model/types';
import { unwrapResult } from '@/shared/api/unwrap';

export type LoginRequest = {
  email: string;
  password: string;
  deviceType: 'WEB';
};

export async function login(request: LoginRequest) {
  const response = await apiClient.post('/api/v1/auth/login', request);
  return unwrapResult<Session>(response);
}

export async function logout(sessionId: string) {
  const response = await apiClient.post('/api/v1/auth/logout', { sessionId });
  return unwrapResult<{ sessionId: string; status: string }>(response);
}
