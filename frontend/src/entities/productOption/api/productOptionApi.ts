import type { ProductOption, ProductOptionItem, ProductOptionStatus } from '@/entities/productOption/model/types';
import { apiClient } from '@/shared/api/apiClient';
import { unwrapResult } from '@/shared/api/unwrap';

export type ProductOptionPayload = {
  name: string;
  status: ProductOptionStatus;
};

export type ProductOptionItemPayload = {
  name: string;
  status: ProductOptionStatus;
};

export async function getProductOptions(categoryId: string, status?: ProductOptionStatus | '') {
  const response = await apiClient.get(`/api/v1/categories/${categoryId}/options`, {
    params: { status: status || undefined }
  });
  return unwrapResult<ProductOption[]>(response);
}

export async function getProductOption(categoryId: string, productOptionId: string) {
  const response = await apiClient.get(`/api/v1/categories/${categoryId}/options/${productOptionId}`);
  return unwrapResult<ProductOption>(response);
}

export async function createProductOption(categoryId: string, request: ProductOptionPayload) {
  const response = await apiClient.post(`/api/v1/categories/${categoryId}/options`, request);
  return unwrapResult<ProductOption>(response);
}

export async function updateProductOption(
  categoryId: string,
  productOptionId: string,
  request: Omit<ProductOptionPayload, 'status'>
) {
  const response = await apiClient.put(`/api/v1/categories/${categoryId}/options/${productOptionId}`, request);
  return unwrapResult<ProductOption>(response);
}

export async function updateProductOptionStatus(
  categoryId: string,
  productOptionId: string,
  request: { status: ProductOptionStatus }
) {
  const response = await apiClient.patch(
    `/api/v1/categories/${categoryId}/options/${productOptionId}/status`,
    request
  );
  return unwrapResult<ProductOption>(response);
}

export async function deleteProductOption(categoryId: string, productOptionId: string) {
  const response = await apiClient.delete(`/api/v1/categories/${categoryId}/options/${productOptionId}`);
  return unwrapResult<null>(response);
}

export async function createProductOptionItem(
  categoryId: string,
  productOptionId: string,
  request: ProductOptionItemPayload
) {
  const response = await apiClient.post(
    `/api/v1/categories/${categoryId}/options/${productOptionId}/items`,
    request
  );
  return unwrapResult<ProductOptionItem>(response);
}

export async function updateProductOptionItem(
  categoryId: string,
  productOptionId: string,
  productOptionItemId: string,
  request: Omit<ProductOptionItemPayload, 'status'>
) {
  const response = await apiClient.put(
    `/api/v1/categories/${categoryId}/options/${productOptionId}/items/${productOptionItemId}`,
    request
  );
  return unwrapResult<ProductOptionItem>(response);
}

export async function updateProductOptionItemStatus(
  categoryId: string,
  productOptionId: string,
  productOptionItemId: string,
  request: { status: ProductOptionStatus }
) {
  const response = await apiClient.patch(
    `/api/v1/categories/${categoryId}/options/${productOptionId}/items/${productOptionItemId}/status`,
    request
  );
  return unwrapResult<ProductOptionItem>(response);
}

export async function deleteProductOptionItem(
  categoryId: string,
  productOptionId: string,
  productOptionItemId: string
) {
  const response = await apiClient.delete(
    `/api/v1/categories/${categoryId}/options/${productOptionId}/items/${productOptionItemId}`
  );
  return unwrapResult<null>(response);
}
