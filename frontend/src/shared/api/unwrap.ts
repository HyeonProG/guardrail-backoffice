import type { AxiosError } from 'axios';
import type { ApiResponse } from '@/shared/api/types';

export function unwrapResult<T>(response: { data: ApiResponse<T> }) {
  return response.data.result;
}

export function getApiErrorMessage(error: unknown) {
  const axiosError = error as AxiosError<ApiResponse<null>>;
  return axiosError.response?.data?.message ?? '요청 처리 중 오류가 발생했습니다.';
}
