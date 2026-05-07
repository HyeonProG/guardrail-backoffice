import { apiClient } from '@/shared/api/apiClient';
import { unwrapResult } from '@/shared/api/unwrap';
import type { PageResponse } from '@/shared/api/types';
import type { Product, ProductHistory, ProductStatus } from '@/entities/product/model/types';

export type ProductPayload = {
  categoryId: string;
  name: string;
  description?: string;
  selectedOptionItemIds?: string[];
  actorId: string;
};

export type ProductDescriptionGeneratePayload = {
  actorId: string;
  productName: string;
  categoryName: string;
  optionSummary: string;
  featureKeywords: string[];
};

export async function getProducts(params: {
  page?: number;
  size?: number;
  sort?: string;
  categoryId?: string;
  status?: ProductStatus | '';
  approvedOnly?: boolean;
  myOnly?: boolean;
} = {}) {
  const response = await apiClient.get('/api/v1/products', {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 20,
      sort: params.sort ?? 'createdAt,desc',
      categoryId: params.categoryId || undefined,
      status: params.status || undefined,
      approvedOnly: params.approvedOnly,
      myOnly: params.myOnly
    }
  });
  return unwrapResult<PageResponse<Product>>(response);
}

export async function getProduct(productId: string) {
  const response = await apiClient.get(`/api/v1/products/${productId}`);
  return unwrapResult<Product>(response);
}

export async function createProduct(request: ProductPayload) {
  const response = await apiClient.post('/api/v1/products', request);
  return unwrapResult<Product>(response);
}

export async function updateProduct(productId: string, request: ProductPayload) {
  const response = await apiClient.put(`/api/v1/products/${productId}`, request);
  return unwrapResult<Product>(response);
}

export async function generateProductDescription(productId: string, request: ProductDescriptionGeneratePayload) {
  const response = await apiClient.post(`/api/v1/products/${productId}/description/generate`, request);
  return unwrapResult<Product>(response);
}

export async function updateProductStatus(
  productId: string,
  request: { status: ProductStatus; actorId: string; reason?: string }
) {
  const response = await apiClient.patch(`/api/v1/products/${productId}/status`, request);
  return unwrapResult<Product>(response);
}

export async function deleteProduct(productId: string) {
  const response = await apiClient.delete(`/api/v1/products/${productId}`);
  return unwrapResult<null>(response);
}

export async function getProductHistories(productId: string) {
  const response = await apiClient.get(`/api/v1/products/${productId}/histories`);
  return unwrapResult<ProductHistory[]>(response);
}
