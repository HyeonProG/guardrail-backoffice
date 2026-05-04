export type ApiResponse<T> = {
  isSuccess: boolean;
  message: string;
  code: number;
  result: T;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
