import { z } from 'zod';

export const productSchema = z.object({
  categoryId: z.string().min(1, '카테고리를 선택해 주세요.'),
  name: z.string().min(1, '상품명을 입력해 주세요.'),
  description: z.string().max(2000, '상품 설명은 2000자 이하여야 합니다.').optional().default(''),
  selectedOptionItemIds: z.array(z.string()).optional().default([])
});

export type ProductFormValues = z.infer<typeof productSchema>;
