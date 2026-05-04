import type { ProductHistory, ProductStatus } from '@/entities/product/model/types';

const productStatusLabelMap: Record<ProductStatus, string> = {
  DRAFT: '작성 중',
  PENDING: '승인 대기',
  APPROVED: '승인 완료',
  REJECTED: '반려',
  INACTIVE: '운영 중지'
};

const productHistoryTypeLabelMap: Record<ProductHistory['type'], string> = {
  CREATED: '등록',
  SUBMITTED: '승인 요청',
  APPROVED: '승인 완료',
  REJECTED: '반려',
  INACTIVATED: '운영 중지',
  UPDATED: '수정'
};

export function getProductStatusLabel(status: ProductStatus) {
  return productStatusLabelMap[status] ?? status;
}

export function getProductHistoryTypeLabel(type: ProductHistory['type']) {
  return productHistoryTypeLabelMap[type] ?? type;
}
