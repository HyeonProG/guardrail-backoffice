import { apiClient } from '@/shared/api/apiClient';
import { unwrapResult } from '@/shared/api/unwrap';
import type {
  ChangePasswordResponse,
  PasswordHistory,
  User,
  UserRole
} from '@/entities/user/model/types';

export async function getUser(userId: string) {
  const response = await apiClient.get(`/api/v1/users/${userId}`);
  return unwrapResult<User>(response);
}

export async function updateUser(
  userId: string,
  request: {
    email: string;
    name: string;
    role: UserRole;
  }
) {
  const response = await apiClient.put(`/api/v1/users/${userId}`, request);
  return unwrapResult<User>(response);
}

export async function getPasswordHistories(userId: string) {
  const response = await apiClient.get(`/api/v1/auth/users/${userId}/password-histories`);
  return unwrapResult<PasswordHistory[]>(response);
}

export async function changePassword(
  userId: string,
  request: {
    currentPassword: string;
    newPassword: string;
  }
) {
  const response = await apiClient.patch(`/api/v1/auth/users/${userId}/password`, request);
  return unwrapResult<ChangePasswordResponse>(response);
}
