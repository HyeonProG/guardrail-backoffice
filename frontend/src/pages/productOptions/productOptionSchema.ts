import { z } from 'zod';

export const productOptionSchema = z.object({
  name: z.string().min(1, '옵션 이름을 입력해 주세요.')
});

export const productOptionItemSchema = z.object({
  name: z.string().min(1, '선택 항목 이름을 입력해 주세요.')
});

export type ProductOptionFormValues = z.infer<typeof productOptionSchema>;
export type ProductOptionItemFormValues = z.infer<typeof productOptionItemSchema>;
