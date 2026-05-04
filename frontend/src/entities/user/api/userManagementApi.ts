import { apiClient } from '@/shared/api/apiClient';
import { unwrapResult } from '@/shared/api/unwrap';
import type { PageResponse } from '@/shared/api/types';
import type {
  TemporaryPasswordIssueResponse,
  User,
  UserRole,
  UserStatus
} from '@/entities/user/model/types';

export type UserCreateResult = User & {
  initialPassword: string;
};

export async function getUsers(params: { page?: number; size?: number; status?: UserStatus | '' } = {}) {
  const response = await apiClient.get('/api/v1/users', {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 50,
      status: params.status || undefined
    }
  });
  return unwrapResult<PageResponse<User>>(response);
}

export async function createUser(request: { email: string; name: string; role: UserRole }) {
  const response = await apiClient.post('/api/v1/users', request);
  return unwrapResult<UserCreateResult>(response);
}

export async function updateUserStatus(userId: string, status: UserStatus) {
  const response = await apiClient.patch(`/api/v1/users/${userId}/status`, { status });
  return unwrapResult<User>(response);
}

export async function issueTemporaryPassword(userId: string) {
  const response = await apiClient.post(`/api/v1/users/${userId}/temporary-password`);
  return unwrapResult<TemporaryPasswordIssueResponse>(response);
}
