import { z } from 'zod';

export const productOptionSchema = z.object({
  name: z.string().min(1, '옵션 이름을 입력해 주세요.')
});

export const productOptionItemSchema = z.object({
  name: z.string().min(1, '선택 항목 이름을 입력해 주세요.'),
  additionalPrice: z.coerce.number().int().min(0, '추가 금액은 0 이상이어야 합니다.')
});

export type ProductOptionFormValues = z.infer<typeof productOptionSchema>;
export type ProductOptionItemFormValues = z.infer<typeof productOptionItemSchema>;
