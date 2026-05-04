import { apiClient } from '@/shared/api/apiClient';
import { unwrapResult } from '@/shared/api/unwrap';
import type { PageResponse } from '@/shared/api/types';
import type { Category, CategoryStatus } from '@/entities/category/model/types';

export type CategoryListParams = {
  page?: number;
  size?: number;
  sort?: string;
  parentId?: string;
  status?: CategoryStatus | '';
};

const emptyToNull = (value: string) => (value.trim() ? value : null);

export async function getCategories(params: CategoryListParams = {}) {
  const response = await apiClient.get('/api/v1/categories', {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 20,
      sort: params.sort ?? 'createdAt,desc',
      parentId: params.parentId || undefined,
      status: params.status || undefined
    }
  });
  return unwrapResult<PageResponse<Category>>(response);
}

export async function getDeletedCategories() {
  const response = await apiClient.get('/api/v1/categories/deleted');
  return unwrapResult<Category[]>(response);
}

export async function createCategory(request: { parentId: string; name: string }) {
  const response = await apiClient.post('/api/v1/categories', {
    parentId: emptyToNull(request.parentId),
    name: request.name
  });
  return unwrapResult<Category>(response);
}

export async function updateCategory(categoryId: string, request: { parentId: string; name: string }) {
  const response = await apiClient.put(`/api/v1/categories/${categoryId}`, {
    parentId: emptyToNull(request.parentId),
    name: request.name
  });
  return unwrapResult<Category>(response);
}

export async function updateCategoryStatus(categoryId: string, status: CategoryStatus) {
  const response = await apiClient.patch(`/api/v1/categories/${categoryId}/status`, { status });
  return unwrapResult<Category>(response);
}

export async function deleteCategory(categoryId: string) {
  const response = await apiClient.delete(`/api/v1/categories/${categoryId}`);
  return unwrapResult<null>(response);
}

export async function restoreCategory(categoryId: string) {
  const response = await apiClient.patch(`/api/v1/categories/${categoryId}/restore`);
  return unwrapResult<Category>(response);
}
